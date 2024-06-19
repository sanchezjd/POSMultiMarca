package com.mycompany.posmultimarca.POS.Newland

import android.content.Context
import android.os.Handler
import android.util.Log
import com.mycompany.posmultimarca.POS.POSController
import com.mycompany.posmultimarca.POS.POSMessenger
import com.newland.me.ConnUtils
import com.newland.me.DeviceManager
import com.newland.mtype.ConnectionCloseEvent
import com.newland.mtype.ModuleType
import com.newland.mtype.conn.DeviceConnParams
import com.newland.mtype.event.DeviceEventListener
import com.newland.mtype.module.common.printer.Printer
import com.newland.mtype.module.common.security.K21SecurityModule
import com.newland.mtype.util.ISOUtils
import com.newland.mtypex.nseries3.NS3ConnParams
import com.mycompany.posmultimarca.BuildConfig
import com.newland.mtype.common.PermissionCode.NEWLAND
import com.newland.mtype.module.common.pin.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class NewlandController(var posMessenger: POSMessenger) : POSController {

    private  val K21_DRIVER_NAME: String = "com.newland.me.K21Driver"
    private var deviceConnParams: DeviceConnParams? = null

    private  lateinit var deviceManager: DeviceManager
    private  lateinit var securityModule: K21SecurityModule
    private  lateinit var printerModule: Printer
    private  lateinit var pininputModule: K21Pininput

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
                //val emvModule = deviceManager.device.getExModule(ExModuleType.EMVINNERLEVEL2)
                Thread.sleep(1000)
                securityModule = deviceManager.device.getStandardModule(ModuleType.COMMON_SECURITY) as K21SecurityModule
                printerModule = deviceManager.device.getStandardModule(ModuleType.COMMON_PRINTER) as Printer
                pininputModule = deviceManager.device.getStandardModule(ModuleType.COMMON_PININPUT) as K21Pininput
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