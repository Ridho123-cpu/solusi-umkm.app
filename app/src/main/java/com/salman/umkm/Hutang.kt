package com.salman.umkm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hutang")
data class Hutang(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val namaPelanggan: String,
    val totalHutang: Double,
    val sisaHutang: Double,
    val dibayar: Double = 0.0,
    val status: String = "BELUM LUNAS", // BELUM LUNAS, LUNAS
    val tanggal: Long = System.currentTimeMillis(),
    val strukId: String
)
