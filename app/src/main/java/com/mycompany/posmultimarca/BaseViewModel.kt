package com.mycompany.posmultimarca

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {

    lateinit var context: Context
    lateinit var activityReference: ComponentActivity

    var global = Global.instance

    open fun inicializarViewModel(contextIn: Context, activityReferenceIn: ComponentActivity) {
        context = contextIn
        activityReference = activityReferenceIn
    }



}