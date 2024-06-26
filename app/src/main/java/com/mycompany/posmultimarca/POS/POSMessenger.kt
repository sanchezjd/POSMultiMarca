package com.mycompany.posmultimarca.POS

interface POSMessenger {
    fun onInitPOSOK()
    fun onInitPOSFail()

    fun onCardDetect(typeCard: TYPE_CARD)

    fun selectAPP(listAPP:ArrayList<String>, onAPPSelect:(Int) -> Unit)

}