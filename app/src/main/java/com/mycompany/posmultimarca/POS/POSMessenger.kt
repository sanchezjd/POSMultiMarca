package com.mycompany.posmultimarca.POS

interface POSMessenger {
    fun onInitPOSOK()
    fun onInitPOSFail()
    fun onCardDetect(typeCard: TYPE_CARD)
    fun selectAPP(listAPP:ArrayList<String>, onAPPSelect:(Int) -> Unit)

    // AGREGADAS NUEVAS
    fun onCardInfoRead(cardMap:HashMap<String,String>,onViewInteraction:(String) -> Unit)
    fun onError(mensaje:String)

    fun onSpecialCase(mensaje:String)
    fun showPinPad(pinType:Int,onPinOk: (String) -> Unit,  onPinNOk:(Int) -> Unit)

}