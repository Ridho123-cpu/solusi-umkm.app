package com.salman.umkm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "belanja")
data class Belanja(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val item: String,
    val isChecked: Boolean = false
)
