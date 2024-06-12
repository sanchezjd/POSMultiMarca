package com.mycompany.posmultimarca.Persistencia

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class DataSharedPreferences(var context: Context) {

    companion object {
        val FIELD_INIT_COMPLETE = "FIELD_INIT_COMPLETE"
        val FIELD_FECHA_ULTIMOCIERRE = "FIELD_FECHA_ULTIMOCIERRE"

    }

    private val preferencesName = "SharedPreferences"


    var sharedPreferences: SharedPreferences? = null

    init {
        initEncryptedSharedPreferences()
    }

    private fun initEncryptedSharedPreferences() {
        try {
            val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            sharedPreferences = EncryptedSharedPreferences.create(
                preferencesName,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.i("K9", "Test")
        }
    }

    private fun resetSharedPreferences() {
        context!!.getSharedPreferences(preferencesName, AppCompatActivity.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun deleteConfigData() {
        resetSharedPreferences()
        initEncryptedSharedPreferences()
    }

    fun updateField(field:String,newValue: String) {
        if (sharedPreferences == null)
            initEncryptedSharedPreferences()
        sharedPreferences!!.edit()
            .putString(field, newValue)
            .apply()
    }

    fun deleteField(field:String) {
        updateField(field,"")
    }

    fun getField(field:String): String {
        if (sharedPreferences == null)
            initEncryptedSharedPreferences()
        return sharedPreferences!!.getString(field, "")!!
    }

    fun existField(field:String): Boolean {
        return !getField(field).equals("")
    }

}