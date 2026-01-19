package com.salman.umkm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produk")
data class Produk(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nama: String,
    val hargaBeli: Double,
    val hargaJual: Double,
    val stok: Double,
    val satuan: String = "pcs"
)
