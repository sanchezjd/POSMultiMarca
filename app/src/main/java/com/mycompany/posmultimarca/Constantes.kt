package com.mycompany.posmultimarca

abstract class Constantes {


    companion object {



        val FIELD_AID = "FIELD_AID"
        val FIELD_PAN = "FIELD_PAN"
        val FIELD_APPLABEL = "FIELD_APPLABEL"
        val FIELD_UPN = "FIELD_UPN"
        val FIELD_FECHA = "FIELD_FECHA"
        val FIELD_HORA = "FIELD_HORA"


    }

    enum class TipoPOS { MPOS, SMARTPOS, TAPONPHONE, QR }

    enum class ModeloPOS { INIT, QPOS, EPAY600, SUNMI, FEITIAN, CENTERM }



}