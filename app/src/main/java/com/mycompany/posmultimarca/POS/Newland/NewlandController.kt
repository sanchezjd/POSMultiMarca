package com.mycompany.posmultimarca.POS.Newland

import android.content.Context
import android.os.Handler
import android.util.Log
import com.mycompany.posmultimarca.BuildConfig
import com.mycompany.posmultimarca.Constantes
import com.mycompany.posmultimarca.POS.*
import com.newland.me.ConnUtils
import com.newland.me.DeviceManager
import com.newland.mtype.ConnectionCloseEvent
import com.newland.mtype.ExModuleType
import com.newland.mtype.ModuleType
import com.newland.mtype.common.InnerProcessingCode
import com.newland.mtype.common.ProcessingCode
import com.newland.mtype.conn.DeviceConnParams
import com.newland.mtype.event.DeviceEventListener
import com.newland.mtype.module.common.cardreader.CommonCardType
import com.newland.mtype.module.common.cardreader.K21CardReader
import com.newland.mtype.module.common.cardreader.K21CardReaderEvent
import com.newland.mtype.module.common.cardreader.SearchCardRule
import com.newland.mtype.module.common.emv.EmvModule
import com.newland.mtype.module.common.emv.EmvTransController
import com.newland.mtype.module.common.emv.EmvTransInfo
import com.newland.mtype.module.common.emv.EmvTransInfo.AIDSelect
import com.newland.mtype.module.common.emv.level2.EmvCardholderCertType
import com.newland.mtype.module.common.emv.level2.EmvFinalAppSelectListener
import com.newland.mtype.module.common.pin.*
import com.newland.mtype.module.common.printer.Printer
import com.newland.mtype.module.common.security.K21SecurityModule
import com.newland.mtype.util.ISOUtils
import com.newland.mtypex.nseries3.NS3ConnParams
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.math.BigDecimal
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

    private var stan: Long = 0
    private var CurrencyCode: String = ""
    private var TerminalCountryCode:String = ""
    private var serial: String = ""

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
        serial = deviceInfo.sn
        return serial
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



    private val mEMVListener: EmvFinalAppSelectListener = object : EmvFinalAppSelectListener {
        override fun onRequestSelectApplication(emvTransController: EmvTransController?, p1: EmvTransInfo?) {
            val map: Map<ByteArray, AIDSelect> = p1!!.getAidSelectMap()
            Log.i(TAG, "onRequestSelectApplication")

            var nameList = ArrayList<String>()
            var aidList = ArrayList<ByteArray>()

            for ( entry in map.entries) {
                nameList.add(entry.value.name)
                aidList.add(entry.value.aid)
            }

            posMessenger.selectAPP(nameList,
                { indiceAid ->
                    if(indiceAid > -1)
                        emvTransController!!.selectApplication( aidList.get(indiceAid))
                    else
                        emvTransController!!.cancelEmv()
                }
            )

        }

        //PUEDO PEDIR CONFIRMACION DEL USUARIO PARA CONFIRMAR LA TRANSACCION
        override fun onRequestTransferConfirm(emvTransController: EmvTransController?, p1: EmvTransInfo?) {
            Log.i(TAG, "onRequestTransferConfirm")

            var aid = ISOUtils.hexString(p1!!.getAid())
            var panActual = p1!!.cardNo;
            var appLabel = p1!!.application_label
            var track2 = ISOUtils.hexString(p1!!.track_2_eqv_data)

            var fechaTrans = p1!!.transactionDate


           val tagList = intArrayOf(0x9F37, 0x9F21, 0x8F)
            val data = emvModule.fetchEmvData(tagList)
            val tlv = ISOUtils.newTlvPackage()
            tlv.unpack(ISOUtils.hex2byte(data))

            var UPN = tlv.getString(0x9F37)
            var horaTrans = tlv.getString(0x9F21)
            var capkIndex = tlv.getString(0x8F) //INDICE DEL CERTIFICADO SOLICTADO

            var mapCardField = HashMap<String,String>()
            mapCardField.put(Constantes.FIELD_AID,aid)  //TAG 4F
            mapCardField.put(Constantes.FIELD_PAN,panActual) //PAN emascarado
            mapCardField.put(Constantes.FIELD_APPLABEL,appLabel) //TAG 4F
            mapCardField.put(Constantes.FIELD_UPN,UPN) //TAG 9F37
            mapCardField.put(Constantes.FIELD_FECHA,fechaTrans) //TAG 9A
            mapCardField.put(Constantes.FIELD_HORA,horaTrans) //TAG 9F21*/

            posMessenger.onCardInfoRead(mapCardField,
                {_ ->
                    emvTransController!!.transferConfirm(true)
                }
            )


        }

        override fun onRequestAmountEntry(emvTransController: EmvTransController?, p1: EmvTransInfo?) {
            Log.i(TAG, "onRequestAmountEntry")
        }

        override fun onRequestPinEntry(emvTransController: EmvTransController?, p1: EmvTransInfo?) {
            Log.i(TAG, "onRequestPinEntry")
        }

        override fun onRequestOnline(emvTransController: EmvTransController?, p1: EmvTransInfo?) {
           Log.i(TAG, "onRequestOnline")
        }

        override fun onEmvFinished(p0: Boolean, emvTransInfo: EmvTransInfo) {

            Log.i(TAG, "onEmvFinished: " + emvTransInfo.executeRslt )
        }

        override fun onFallback(p0: EmvTransInfo?) {
            Log.i(TAG, "onFallback")
            posMessenger.onError("Tarjeta no permitida")
        }

        override fun onError(emvTransController: EmvTransController?, p1: java.lang.Exception?) {
            posMessenger.onError(p1.toString())
            Log.i(TAG, "onError")
        }

        override fun isAccountTypeSelectInterceptor(): Boolean {
            Log.i(TAG, "isAccountTypeSelectInterceptor")
            return true
        }

        override fun isCardHolderCertConfirmInterceptor(): Boolean {
            Log.i(TAG, "isCardHolderCertConfirmInterceptor")
            return true
        }

        override fun isEcSwitchInterceptor(): Boolean {
            Log.i(TAG, "isEcSwitchInterceptor")
            return true
        }

        override fun isTransferSequenceGenerateInterceptor(): Boolean {
            Log.i(TAG, "isTransferSequenceGenerateInterceptor")
            return false
        }

        override fun isLCDMsgInterceptor(): Boolean {
            Log.i(TAG, "isLCDMsgInterceptor")
            return true
        }

        override fun accTypeSelect(): Int {
            Log.i(TAG, "accTypeSelect")
            return 0
        }

        override fun cardHolderCertConfirm(p0: EmvCardholderCertType?, p1: String?): Boolean {
            Log.i(TAG, "cardHolderCertConfirm")
            return true
        }

        override fun ecSwitch(): Int {
            Log.i(TAG, "ecSwitch")
            return 0
        }

        override fun incTsc(): Int {
            Log.i(TAG, "incTsc")
            return 0
        }

        override fun isLanguageselectInterceptor(): Boolean {
            Log.i(TAG, "isLanguageselectInterceptor")
            return true
        }

        override fun lcdMsg(p0: String?, p1: String?, p2: Boolean, p3: Int): Int {
            Log.i(TAG, "lcdMsg")
            return 0
        }

        override fun languageSelect(p0: ByteArray?, p1: Int): ByteArray {
            Log.i(TAG, "languageSelect")
            return ByteArray(0)
        }

        override fun onFinalAppSelect(emvTransController: EmvTransController?, p1: EmvTransInfo?) {
            Log.i(TAG, "onFinalAppSelect")

            var serialLocal = serial
            var stanStr = stan.toString().padStart(8,'0')

            if( serialLocal.equals(""))
                serialLocal = getSerial()


            if(serialLocal.length < 8)
                serialLocal = serialLocal.padStart(8, '0')
            else  if(serialLocal.length >= 8)
                serialLocal = serialLocal.substring(serialLocal.length-8)

            //You can set emv kernel data in this step
            emvModule.setEmvData(0x9F1A, ISOUtils.hex2byte(TerminalCountryCode))
            emvModule.setEmvData(0x9F03, ISOUtils.hex2byte("000000000000"))
            emvModule.setEmvData(0x5F2A, ISOUtils.hex2byte(CurrencyCode))

            emvModule.setEmvData(0x9F41, ISOUtils.hex2byte(stanStr))
            emvModule.setEmvData(0x9F1E, serialLocal.encodeToByteArray())
            emvModule.setEmvData(0x9F53, ISOUtils.hex2byte("52"))

            emvTransController!!.transferConfirm(true)

        }

    }

    override fun initTransaction(
        amount: Long,
        typeTrans: TYPE_TRANSACTION,
        swipeAllow: Boolean,
        iccAllow: Boolean,
        rfcAllow: Boolean,
        timeOut: Long,
        stan: Long,
        CurrencyCode: String,
        TerminalCountryCode:String
    ) {

        var readerList = arrayListOf<ModuleType>()

        if(swipeAllow) readerList.add(ModuleType.COMMON_SWIPER)
        if(iccAllow) readerList.add(ModuleType.COMMON_ICCARDREADER)
        if(rfcAllow) readerList.add(ModuleType.COMMON_RFCARDREADER)


        this.stan = stan
        this.CurrencyCode = CurrencyCode
        this.TerminalCountryCode = TerminalCountryCode


        cardReader.openCardReader(readerList.toTypedArray(), false, timeOut, TimeUnit.SECONDS,

            object : DeviceEventListener<K21CardReaderEvent> {
                override fun onEvent(p0: K21CardReaderEvent?, handler: Handler?) {
                    Log.i(TAG, p0.toString())

                    if(p0!!.isSuccess) {
                      if(p0.openCardReaderResult.responseCardTypes[0] == CommonCardType.ICCARD) {
                          posMessenger.onCardDetect(TYPE_CARD.ICC)

                          val transType = InnerProcessingCode.USING_STANDARD_PROCESSINGCODE
                          var  controller = emvModule.getEmvTransController(mEMVListener)

                          if (controller != null) {
                              controller.startEmv(
                                  ProcessingCode.GOODS_AND_SERVICE,
                                  transType,
                                  BigDecimal(amount.toString()),
                                  BigDecimal("0"),
                                  false,
                                  false
                              )
                          }
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
        for(aidStr in EMVValues.LIST_AID_CONTACT) {
            var result = emvModule.addAIDWithDataSource(ISOUtils.hex2byte(aidStr))


            Log.i(TAG,"AID CONTACT $i: $result")
            i++
        }
    }

    private fun loadCAPK() {
        emvModule.clearAllCAPublicKey()
        var i=0
        for(capkStr in EMVValues.LIST_CAPK) {
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