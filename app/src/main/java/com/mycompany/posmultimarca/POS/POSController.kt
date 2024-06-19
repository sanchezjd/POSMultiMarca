package com.mycompany.posmultimarca.POS

import android.content.Context

interface POSController {
    fun initPOS(context: Context)
    fun getSerial():String

    fun setMKDevelopmentMode(mkey:String, indice:Int, chk:String?):Boolean
    fun setEWK(ewkey:String, indiceMK: Int,indice: Int, chk:String):Boolean

    fun firstExecute()



}