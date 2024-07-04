package com.mycompany.posmultimarca.POS

import android.content.Context

enum class TYPE_TRANSACTION  {VENTA, ANULACION}

enum class TYPE_CARD  {MAG, ICC, RFC, QR}

interface POSController {
    fun initPOS(context: Context)
    fun getSerial():String

    fun setMKDevelopmentMode(mkey:String, indice:Int, chk:String?):Boolean
    fun setEWK(ewkey:String, indiceMK: Int,indice: Int, chk:String):Boolean

    fun firstExecute()



    fun initTransaction(amount:Long,
                        typeTrans:TYPE_TRANSACTION,
                        swipeAllow:Boolean,
                        iccAllow:Boolean,
                        rfcAllow:Boolean,
                        timeOut:Long,
                        stan: Long,
                        CurrencyCode: String,
                        TerminalCountryCode:String)

    fun cancelWaitCard()


}