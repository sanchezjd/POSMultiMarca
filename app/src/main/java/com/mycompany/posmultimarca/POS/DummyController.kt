package com.mycompany.posmultimarca.POS

import android.content.Context

class DummyController(var posMessenger: POSMessenger) : POSController {

    override fun initPOS(context: Context) {
        posMessenger.onInitPOSOK()
    }

    override fun getSerial(): String {
        return "DUMMYSERIAL"
    }
}