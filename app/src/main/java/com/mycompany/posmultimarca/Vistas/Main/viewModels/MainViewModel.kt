package com.mycompany.posmultimarca.Vistas.Main.viewModels

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycompany.posmultimarca.BaseViewModel
import com.mycompany.posmultimarca.POS.Newland.NewlandController
import com.mycompany.posmultimarca.Persistencia.DataSharedPreferences

class MainViewModel: BaseViewModel() {

    var appPrimeraVez by mutableStateOf(false)
    fun appIniciada() {
        DataSharedPreferences(context).updateField(DataSharedPreferences.FIELD_INIT_COMPLETE,"1")
    }

    override fun inicializarViewModel(contextIn: Context, activityReferenceIn: ComponentActivity) {
        super.inicializarViewModel(contextIn, activityReferenceIn)
        if(!DataSharedPreferences(context).getField(DataSharedPreferences.FIELD_INIT_COMPLETE).equals("1")) {
            appPrimeraVez = true
            appIniciada()
        }

        global.setPOSController(NewlandController())

       // DataSharedPreferences(context).updateField(DataSharedPreferences.FIELD_FECHA_ULTIMOCIERRE,"0")
    }
}