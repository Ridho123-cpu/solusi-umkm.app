package com.salman.umkm

import com.salman.umkm.Struk
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProdukDao {
    @Query("SELECT * FROM produk")
    fun getAllProduk(): Flow<List<Produk>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduk(produk: Produk)

    @Delete
    suspend fun deleteProduk(produk: Produk)

    @Query("UPDATE produk SET stok = stok - :jumlah WHERE id = :produkId AND stok >= :jumlah")
    suspend fun kurangiStok(produkId: Int, jumlah: Double): Int

    @Insert
    suspend fun insertTransaksi(transaksi: Transaksi)

    @Query("""
    SELECT 
    (SELECT IFNULL(SUM(totalHarga), 0) FROM transaksi WHERE isLunas = 1 AND isHutang = 0) + 
    (SELECT IFNULL(SUM(dibayar), 0) FROM hutang)
""")
    fun getTotalPendapatan(): Flow<Double?>


    @Query("SELECT * FROM produk WHERE id = :id")
    suspend fun getProdukById(id: Int): Produk?

    @Query("SELECT * FROM transaksi ORDER BY tanggal DESC")
    fun getRiwayatTransaksi(): kotlinx.coroutines.flow.Flow<List<Transaksi>>

    @Query("""
    SELECT 
    (SELECT IFNULL(SUM(laba), 0) FROM transaksi WHERE isLunas = 1 AND isHutang = 0) + 
    (SELECT IFNULL(SUM(t.laba * (h.dibayar / NULLIF(h.totalHutang, 0))), 0) 
     FROM transaksi t 
     JOIN hutang h ON t.strukId = h.strukId 
     WHERE t.isHutang = 1)
""")
    fun getTotalLaba(): Flow<Double?>

    @Query("DELETE FROM transaksi WHERE id = :id")
    suspend fun hapusTransaksi(id: String)

    @Query("DELETE FROM transaksi")
    suspend fun hapusSemuaTransaksi()

    @Query("SELECT * FROM belanja")
    fun getAllBelanja(): kotlinx.coroutines.flow.Flow<List<Belanja>>

    @Insert
    suspend fun insertBelanja(belanja: Belanja)

    @Update
    suspend fun updateBelanja(belanja: Belanja)

    @Delete
    suspend fun deleteBelanja(belanja: Belanja)

    @Query("DELETE FROM belanja WHERE isChecked = 1")
    suspend fun hapusBelanjaTercentang()

    @Insert
    suspend fun insertStok(struk: Struk)

    @Query("SELECT * FROM struk ORDER BY tanggal DESC")
    fun getAllStruk(): Flow<List<Struk>>

    @Query("SELECT * FROM transaksi WHERE strukId = :strukId")
    suspend fun getDetailTransaksiByStruk(strukId: String): List<Transaksi>

    // Tambahkan di ProdukDao untuk menghapus struk sekaligus detailnya
    @Query("DELETE FROM struk WHERE id = :strukId")
    suspend fun hapusStruk(strukId: String)

    @Query("DELETE FROM transaksi WHERE strukId = :strukId")
    suspend fun hapusTransaksiByStruk(strukId: String)

    // Analisis Produk Terlaris (Top 5)
    @Query("SELECT namaProduk FROM transaksi GROUP BY namaProduk ORDER BY SUM(jumlah) DESC LIMIT 5")
    fun getProdukTerlaris(): Flow<List<String>>

    // Hutang
    @Query("SELECT * FROM hutang ORDER BY tanggal DESC")
    fun getAllHutang(): Flow<List<Hutang>>

    @Insert
    suspend fun insertHutang(hutang: Hutang)

    @Update
    suspend fun updateHutang(hutang: Hutang)

    @Delete
    suspend fun deleteHutang(hutang: Hutang)

    @Query("UPDATE transaksi SET isLunas = 1 WHERE strukId = :strukId")
    suspend fun setTransaksiLunas(strukId: String)

    @Query("DELETE FROM produk") suspend fun clearProduk()
    @Query("DELETE FROM transaksi") suspend fun clearTransaksi()
    @Query("DELETE FROM struk") suspend fun clearStruk()
    @Query("DELETE FROM hutang") suspend fun clearHutang()
    @Query("DELETE FROM belanja") suspend fun clearBelanja()

}
