package com.mycompany.posmultimarca.POS.Newland

import android.content.Context
import android.os.Handler
import android.util.Log
import com.mycompany.posmultimarca.BuildConfig
import com.mycompany.posmultimarca.POS.*
import com.newland.me.ConnUtils
import com.newland.me.DeviceManager
import com.newland.mtype.ConnectionCloseEvent
import com.newland.mtype.ExModuleType
import com.newland.mtype.ModuleType
import com.newland.mtype.conn.DeviceConnParams
import com.newland.mtype.event.DeviceEventListener
import com.newland.mtype.module.common.cardreader.CommonCardType
import com.newland.mtype.module.common.cardreader.K21CardReader
import com.newland.mtype.module.common.cardreader.K21CardReaderEvent
import com.newland.mtype.module.common.cardreader.SearchCardRule
import com.newland.mtype.module.common.emv.EmvModule
import com.newland.mtype.module.common.pin.*
import com.newland.mtype.module.common.printer.Printer
import com.newland.mtype.module.common.security.K21SecurityModule
import com.newland.mtype.util.ISOUtils
import com.newland.mtypex.nseries3.NS3ConnParams
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.concurrent.TimeUnit


class NewlandController(var posMessenger: POSMessenger) : POSController {

    private  val K21_DRIVER_NAME: String = "com.newland.me.K21Driver"
    private var deviceConnParams: DeviceConnParams? = null

    private  lateinit var deviceManager: DeviceManager
    private  lateinit var securityModule: K21SecurityModule
    private  lateinit var printerModule: Printer
    private  lateinit var pininputModule: K21Pininput
    private lateinit var emvModule: EmvModule


    private lateinit var cardReader: K21CardReader



    private val TAG = "NewlandController"

    override fun initPOS(context: Context) {
        GlobalScope.async {
            try {
                deviceManager = ConnUtils.getDeviceManager()
                deviceConnParams = NS3ConnParams()
                deviceManager.init(
                    context,
                    K21_DRIVER_NAME,
                    deviceConnParams,
                    object : DeviceEventListener<ConnectionCloseEvent> {
                        override fun onEvent(event: ConnectionCloseEvent, handler: Handler) {
                            if (event.isSuccess) {

                            }
                            if (event.isFailed) {
                                posMessenger.onInitPOSFail()
                            }
                        }
                        override fun getUIHandler(): Handler? {
                            return null
                        }
                    })
                deviceManager.connect()
                securityModule = deviceManager.device.getStandardModule(ModuleType.COMMON_SECURITY) as K21SecurityModule
                printerModule = deviceManager.device.getStandardModule(ModuleType.COMMON_PRINTER) as Printer
                pininputModule = deviceManager.device.getStandardModule(ModuleType.COMMON_PININPUT) as K21Pininput

                emvModule = deviceManager.device.getExModule(ExModuleType.EMVINNERLEVEL2) as EmvModule
                emvModule.initEmvModule(context)

                cardReader = deviceManager.device.getStandardModule(ModuleType.COMMON_CARDREADER) as K21CardReader

                loadCAPK()
                loadAIDContact() //VA A CAMBIAR PARA CUANDO SE DETECTE EL TIPO TARJETA

                posMessenger.onInitPOSOK()

            } catch (e: Exception) {
                posMessenger.onInitPOSFail()
            }
        }
    }

    override fun getSerial(): String {

        var deviceInfo = securityModule.deviceInfo//securityModule.deviceInfo
        var SerialNumber = deviceInfo.sn
        return SerialNumber
    }

    override fun setMKDevelopmentMode(mkey: String, indice: Int, chk:String?):Boolean {
        var result = false
        try {
            var arrayResult = pininputModule.loadMainKey(
                KekUsingType.ENCRYPT_TMK,
                indice,
                ISOUtils.hex2byte(mkey),
                if(chk!=null) ISOUtils.hex2byte(chk) else null,
                -1
            )
            result =
                (arrayResult != null && arrayResult.contentEquals(byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00)))

        }
        catch (e: Exception) {
            result = false
        }
        return result
    }

    override fun setEWK(ewkey: String, indiceMK: Int,indice: Int, chk:String):Boolean  {
        var arrayResult = pininputModule.loadWorkingKey(WorkingKeyType.PININPUT, indiceMK,indice,ISOUtils.hex2byte(ewkey),ISOUtils.hex2byte(chk))
        return (arrayResult != null && arrayResult.contentEquals(byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00)))
    }

    override fun firstExecute() {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            setMKDevelopmentMode(
                "1C0F0AA15653A29AA354B6D9C7F9196C",
                1,
                "6D0C73F1"
            ) //PLAIN KEY 7DD1904BCC309CDD486FF3B42B7235ED
            // testPINKEYS("6EBEAE98D8F0F605352CB05A933F7DC6", 1,2,"191D0C32" ) //31323334353637383930313233343536
        }
    }

    override fun initTransaction(
        amount: Long,
        typeTrans: TYPE_TRANSACTION,
        swipeAllow: Boolean,
        iccAllow: Boolean,
        rfcAllow: Boolean,
        timeOut: Long
    ) {

        var readerList = arrayListOf<ModuleType>()

        if(swipeAllow) readerList.add(ModuleType.COMMON_SWIPER)
        if(iccAllow) readerList.add(ModuleType.COMMON_ICCARDREADER)
        if(rfcAllow) readerList.add(ModuleType.COMMON_RFCARDREADER)


        cardReader.openCardReader(readerList.toTypedArray(), false, timeOut, TimeUnit.SECONDS,

            object : DeviceEventListener<K21CardReaderEvent> {
                override fun onEvent(p0: K21CardReaderEvent?, handler: Handler?) {
                    Log.i(TAG, p0.toString())

                    if(p0!!.isSuccess) {
                      if(p0.openCardReaderResult.responseCardTypes[0] == CommonCardType.ICCARD) {
                          posMessenger.onCardDetect(TYPE_CARD.ICC)
                      }
                      else  if(p0.openCardReaderResult.responseCardTypes[0] == CommonCardType.RFCARD) {
                          posMessenger.onCardDetect(TYPE_CARD.RFC)
                      }
                    }
                }
                override fun getUIHandler(): Handler? {
                    return null
                }
            }
            , SearchCardRule.RFCARD_FIRST)

    }

    override fun cancelWaitCard() {
        cardReader.cancelCardRead()
    }

    private fun loadAIDContact() {
        emvModule.clearAllAID()
        var i=0
        for(aidStr in Constantes.LIST_AID_CONTACT) {
            var result = emvModule.addAIDWithDataSource(ISOUtils.hex2byte(aidStr))


            Log.i(TAG,"AID CONTACT $i: $result")
            i++
        }
    }

    private fun loadCAPK() {
        emvModule.clearAllCAPublicKey()
        var i=0
        for(capkStr in Constantes.LIST_CAPK) {
           var result = emvModule.addCAPublicKeyWithDataSource(ISOUtils.hex2byte(capkStr))

            Log.i(TAG,"CAPK $i: $result")
            i++
        }
    }

    fun testPINKEYS(ewkey: String, indiceMK: Int,indice: Int, chk:String):Boolean  {
        try {
            var arrayResult = pininputModule.loadWorkingKey(
                WorkingKeyType.DATAENCRYPT,
                indiceMK,
                indice,
                ISOUtils.hex2byte(ewkey),
                ISOUtils.hex2byte(chk)
            )
            if ((arrayResult != null && arrayResult.contentEquals(byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)))) {

                var encAlg =
                    EncryptAlgorithm(EncryptAlgorithm.KeyMode.CBC, EncryptAlgorithm.ManufacturerAlgorithm.STANDARD)
                var encripResult =
                    pininputModule.encryptData(encAlg, WorkingKey(2), ISOUtils.hex2byte("0412546C7ACD9BEB"), byteArrayOf(0,0,0,0,0,0,0,0))


                Log.i(TAG, "OK")
            }
        }
        catch (e: Exception) {
            Log.i(TAG, e.toString())
        }
        return true
    }




}