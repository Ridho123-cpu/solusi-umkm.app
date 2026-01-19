package com.salman.umkm

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("umkm_prefs", Context.MODE_PRIVATE)

    fun saveNamaToko(nama: String) {
        sharedPreferences.edit().putString("nama_toko", nama).apply()
    }

    fun getNamaToko(): String? {
        return sharedPreferences.getString("nama_toko", null)
    }
}
