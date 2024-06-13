package com.mycompany.posmultimarca.POS

import android.content.Context

interface POSController {
    fun initPOS(context: Context)
    fun getSerial():String

}