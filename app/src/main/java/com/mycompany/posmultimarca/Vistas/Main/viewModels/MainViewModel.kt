package com.mycompany.posmultimarca.Vistas.Main.viewModels

import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycompany.posmultimarca.BaseViewModel
import com.mycompany.posmultimarca.Constantes
import com.mycompany.posmultimarca.POS.DummyController
import com.mycompany.posmultimarca.POS.Newland.NewlandController
import com.mycompany.posmultimarca.POS.POSMessenger
import com.mycompany.posmultimarca.POS.TYPE_CARD
import com.mycompany.posmultimarca.POS.TYPE_TRANSACTION
import com.mycompany.posmultimarca.Persistencia.DataSharedPreferences

class MainViewModel: BaseViewModel(),POSMessenger {

    var appPrimeraVez by mutableStateOf(false)
    var mensaje by mutableStateOf("")
    var serial by mutableStateOf("")

    var showMenuAppsID  by mutableStateOf(false)
    var showCardInfo  by mutableStateOf(false)
    var showPinPad  by mutableStateOf(false)

    var numCard by  mutableStateOf("")
    var aidCard by  mutableStateOf("")
    var labelCard by  mutableStateOf("")

    fun appIniciada() {
        DataSharedPreferences(context).updateField(DataSharedPreferences.FIELD_INIT_COMPLETE,"1")
    }

    override fun inicializarViewModel(contextIn: Context, activityReferenceIn: ComponentActivity) {
        super.inicializarViewModel(contextIn, activityReferenceIn)
        if(!DataSharedPreferences(context).getField(DataSharedPreferences.FIELD_INIT_COMPLETE).equals("1")) {
            appPrimeraVez = true
        }

        val fabricante = Build.MANUFACTURER.uppercase()
        val modelo = Build.MODEL.uppercase()

        if(modelo.contains("N910"))
            global.setPOSController(NewlandController(this))
        else
            global.setPOSController(DummyController(this))

        global.posController.initPOS(contextIn)
    }

    override fun onInitPOSOK() {
        appIniciada()
        serial =  global.posController.getSerial()
        if(appPrimeraVez)
            global.posController.firstExecute()
    }

    override fun onInitPOSFail() {
        mensaje = "Fallo POS Init"
    }

    override fun onCardDetect(typeCard: TYPE_CARD) {
        if(typeCard == TYPE_CARD.ICC)
            mensaje = "TARJETA CONTACTO"
        else  if(typeCard == TYPE_CARD.RFC)
            mensaje = "TARJETA SIN CONTACTO"
    }

    var listadoApp = ArrayList<Pair<Int,String>>()
    var onAPPSelectPublic:(Int) -> Unit?  by mutableStateOf( {  })

    override fun selectAPP(listAPP: ArrayList<String>, onAPPSelect: (Int) -> Unit) {
        var i = 0
        for(aidApp in listAPP) {
            listadoApp.add(Pair(i,aidApp))
            i = i + 1
        }
        onAPPSelectPublic = onAPPSelect
        showMenuAppsID = true
    }

    var onViewInteractionPublic:(String) -> Unit?  by mutableStateOf( {  })
    override fun onCardInfoRead(cardMap: HashMap<String, String>, onViewInteraction: (String) -> Unit) {
        onViewInteractionPublic = onViewInteraction
        aidCard = cardMap.get(Constantes.FIELD_AID).toString()
        numCard = cardMap.get(Constantes.FIELD_PAN).toString()
        labelCard = cardMap.get(Constantes.FIELD_APPLABEL).toString()
        showCardInfo = true
    }

    override fun onError(mensaje: String) {
        this.mensaje = mensaje
    }

    override fun onSpecialCase(mensaje: String) {

    }

    var onPinOkView:(String) -> Unit?  by mutableStateOf( {  })
    var onPinNOkView:(Int) -> Unit?  by mutableStateOf( {  })
    override fun showPinPad(pinType: Int, onPinOk: (String) -> Unit, onPinNOk: (Int) -> Unit) {
        onPinOkView = onPinOk
        onPinNOkView = onPinNOk
        showPinPad = true
    }


    fun initSale() {
        mensaje = "ESPERANDO TARJETA"
        global.posController.initTransaction(
            1000,
            TYPE_TRANSACTION.VENTA,
            swipeAllow =  false,
            iccAllow =  true,
            rfcAllow =  true,
            timeOut = 10,
            1,
            "0937",
            "0862")
    }
}