package com.mycompany.posmultimarca.POS

import android.content.Context

class DummyController(var posMessenger: POSMessenger) : POSController {

    override fun initPOS(context: Context) {
        posMessenger.onInitPOSOK()
    }

    override fun getSerial(): String {
        return "DUMMYSERIAL"
    }

    override fun setMKDevelopmentMode(mkey: String, indice: Int, chk: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun setEWK(ewkey: String, indiceMK: Int, indice: Int, chk: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun firstExecute() {
        TODO("Not yet implemented")
    }

    override fun initTransaction(
        amount: Long,
        typeTrans: TYPE_TRANSACTION,
        swipeAllow: Boolean,
        iccAllow: Boolean,
        rfcAllow: Boolean,
        timeOut: Long
    ) {
        TODO("Not yet implemented")
    }

    override fun cancelWaitCard() {
        TODO("Not yet implemented")
    }
}