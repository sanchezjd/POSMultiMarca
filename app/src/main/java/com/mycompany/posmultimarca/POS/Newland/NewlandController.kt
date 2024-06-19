package com.mycompany.posmultimarca.POS.Newland

import android.content.Context
import android.os.Handler
import com.mycompany.posmultimarca.POS.POSController
import com.mycompany.posmultimarca.POS.POSMessenger
import com.newland.me.ConnUtils
import com.newland.me.DeviceManager
import com.newland.mtype.ConnectionCloseEvent
import com.newland.mtype.ModuleType
import com.newland.mtype.conn.DeviceConnParams
import com.newland.mtype.event.DeviceEventListener
import com.newland.mtype.module.common.pin.K21Pininput
import com.newland.mtype.module.common.pin.KekUsingType
import com.newland.mtype.module.common.pin.WorkingKeyType
import com.newland.mtype.module.common.printer.Printer
import com.newland.mtype.module.common.security.K21SecurityModule
import com.newland.mtype.util.ISOUtils
import com.newland.mtypex.nseries3.NS3ConnParams
import com.mycompany.posmultimarca.BuildConfig
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

                if (BuildConfig.BUILD_TYPE.equals("debug")) {
                    setMKDevelopmentMode("253C9D9D7C2FBBFA253C9D9D7C2FBBFA", 1, "82E13665")
                    //testPINKEYS("36610F11F191E51A897B2AB3FC44E0F3", 1,2, chk:String)

                }



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

    override fun setMKDevelopmentMode(mkey: String, indice: Int, chk:String):Boolean {
        var result = false
        try {
            var arrayResult = pininputModule.loadMainKey(
                KekUsingType.ENCRYPT_TMK,
                indice,
                ISOUtils.hex2byte(mkey),
                ISOUtils.hex2byte(chk),
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


    fun testPINKEYS(ewkey: String, indiceMK: Int,indice: Int, chk:String):Boolean  {
        var arrayResult = pininputModule.loadWorkingKey(WorkingKeyType.DATAENCRYPT, indiceMK,indice,ISOUtils.hex2byte(ewkey),ISOUtils.hex2byte(chk))
        return (arrayResult != null && arrayResult.contentEquals(byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00)))
    }


}