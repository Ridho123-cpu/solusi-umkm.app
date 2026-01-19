package com.salman.umkm

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.salman.umkm.Struk


@Database(entities =
    [Produk::class, Transaksi::class, Belanja::class, Struk::class, Hutang::class],
    version = 12,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun produkDao(): ProdukDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "umkm_database"
                )
                    // 3. Tambahkan fallbackToDestructiveMigration() agar database di-reset
                    // saat ada perubahan versi (cocok untuk tahap pengembangan)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

