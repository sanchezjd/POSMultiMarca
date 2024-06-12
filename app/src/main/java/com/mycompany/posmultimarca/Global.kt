package com.mycompany.posmultimarca

import com.mycompany.posmultimarca.POS.POSController

class Global  private constructor() {

    companion object {
        val instance:Global by lazy {
            Global()
        }
    }

    lateinit var posController: POSController

    fun setPOSController(posController: POSController) {
        this.posController = posController
    }

}

