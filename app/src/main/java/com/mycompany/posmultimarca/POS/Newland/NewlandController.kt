package com.mycompany.posmultimarca.POS.Newland

import android.content.Context
import android.os.Handler
import com.mycompany.posmultimarca.POS.POSController
import com.mycompany.posmultimarca.POS.POSMessenger
import com.newland.me.ConnUtils
import com.newland.me.DeviceManager
import com.newland.mtype.ConnectionCloseEvent
import com.newland.mtype.conn.DeviceConnParams
import com.newland.mtype.event.DeviceEventListener
import com.newland.mtypex.nseries3.NS3ConnParams

class NewlandController(var posMessenger: POSMessenger) : POSController {

    private  val K21_DRIVER_NAME: String = "com.newland.me.K21Driver"
    private var deviceConnParams: DeviceConnParams? = null
    private  lateinit var deviceManager: DeviceManager

    override fun initPOS(context: Context) {
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
            posMessenger.onInitPOSOK()
        }
        catch (e: Exception) {
            posMessenger.onInitPOSFail()
        }

    }

    override fun getSerial(): String {
        return deviceManager.device.deviceInfo.sn
    }

}