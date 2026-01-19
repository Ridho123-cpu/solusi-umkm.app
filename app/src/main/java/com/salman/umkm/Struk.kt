package com.salman.umkm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "struk")
data class Struk(
    @PrimaryKey
    val id: String, // Contoh: STR-A1B2C3
    val totalBelanja: Double,
    val tanggal: Long = System.currentTimeMillis()
)
