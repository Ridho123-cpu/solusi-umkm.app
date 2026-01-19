package com.salman.umkm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaksi")
data class Transaksi(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val strukId: String,
    val namaProduk: String,
    val jumlah: Double,
    val totalHarga: Double,
    val laba: Double,
    val tanggal: Long = System.currentTimeMillis(),
    val isHutang: Boolean = false,
    val isLunas: Boolean = true
)
