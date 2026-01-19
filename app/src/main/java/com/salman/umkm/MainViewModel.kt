package com.salman.umkm

import com.salman.umkm.Struk
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID // Pastikan import ini benar

class MainViewModel(private val dao: ProdukDao) : ViewModel() {

    // 1. DATA PRODUK
    val daftarProduk = dao.getAllProduk().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun tambahProduk(nama: String, hBeli: Double, hJual: Double, stok: Double, satuan: String) {
        viewModelScope.launch {
            dao.insertProduk(Produk(nama = nama, hargaBeli = hBeli, hargaJual = hJual, stok = stok, satuan = satuan))
        }
    }

    fun hapusProduk(produk: Produk) {
        viewModelScope.launch {
            dao.deleteProduk(produk)
        }
    }


    val daftarStruk = dao.getAllStruk().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Fungsi Utama Kasir (Sistem Keranjang)
    fun prosesPenjualan(keranjang: List<Pair<Produk, Double>>, sebagaiHutang: Boolean, namaPembeli: String = "") {viewModelScope.launch {
        val idStruk = "STR-${UUID.randomUUID().toString().uppercase().take(8)}"
        val totalSemua = keranjang.sumOf { it.first.hargaJual * it.second }

        // 1. Simpan Struk
        dao.insertStok(Struk(id = idStruk, totalBelanja = totalSemua))

        // 2. Simpan Transaksi dengan status Lunas tergantung metode bayar
        keranjang.forEach { (produk, jml) ->
            val totalHrg = produk.hargaJual * jml
            val untung = (produk.hargaJual - produk.hargaBeli) * jml

            dao.insertTransaksi(
                Transaksi(
                    strukId = idStruk,
                    namaProduk = produk.nama,
                    jumlah = jml,
                    totalHarga = totalHrg,
                    laba = untung,
                    isHutang = sebagaiHutang,
                    isLunas = !sebagaiHutang // Jika hutang, maka isLunas = false
                )
            )
            dao.kurangiStok(produk.id, jml)
        }

        // 3. Jika hutang, tambahkan ke catatan hutang
        if (sebagaiHutang) {
            dao.insertHutang(Hutang(
                namaPelanggan = namaPembeli,
                totalHutang = totalSemua,
                sisaHutang = totalSemua,
                strukId = idStruk // Pastikan model Hutang punya strukId untuk referensi balik
            ))
        }
    }
    }

    fun hapusTransaksi(id: String) {
        viewModelScope.launch {
            // Menghapus struk (Parent)
            dao.hapusStruk(id)
            // Menghapus detail transaksi (Children) agar tidak jadi sampah data
            dao.hapusTransaksiByStruk(id)
        }
    }

    // 3. LAPORAN KEUANGAN
    val totalPendapatan = dao.getTotalPendapatan().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0
    )

    val totalLaba = dao.getTotalLaba().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0.0
    )

    // 4. DAFTAR BELANJA
    val daftarBelanja = dao.getAllBelanja().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun tambahBelanja(nama: String) = viewModelScope.launch { dao.insertBelanja(Belanja(item = nama)) }
    fun updateStatusBelanja(belanja: Belanja, cek: Boolean) = viewModelScope.launch { dao.updateBelanja(belanja.copy(isChecked = cek)) }
    fun hapusBelanja(belanja: Belanja) = viewModelScope.launch { dao.deleteBelanja(belanja) }
    fun hapusYangSelesai() = viewModelScope.launch { dao.hapusBelanjaTercentang() }

    suspend fun getDetailTransaksi(strukId: String): List<Transaksi> {
        return dao.getDetailTransaksiByStruk(strukId)
    }

    val produkTerlaris = dao.getProdukTerlaris().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val daftarHutang = dao.getAllHutang().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun tambahHutang(nama: String, jumlah: Double, strukId: String = "") = viewModelScope.launch {
        dao.insertHutang(Hutang(namaPelanggan = nama, totalHutang = jumlah, sisaHutang = jumlah, strukId = strukId))
    }

    fun cicilHutang(hutang: Hutang, jumlahCicil: Double) = viewModelScope.launch {
        val bayarBaru = hutang.dibayar + jumlahCicil
        val sisaBaru = (hutang.totalHutang - bayarBaru).coerceAtLeast(0.0)
        val statusBaru = if (sisaBaru <= 0.0) "LUNAS" else "BELUM LUNAS"

        dao.updateHutang(hutang.copy(
            dibayar = bayarBaru,
            sisaHutang = sisaBaru,
            status = statusBaru
        ))

        // LOGIKA PENTING: Jika sudah lunas, ubah transaksi menjadi lunas agar masuk Omzet
        if (statusBaru == "LUNAS") {
            dao.setTransaksiLunas(hutang.strukId)
        }
    }

    fun hapusHutang(hutang: Hutang) = viewModelScope.launch { dao.deleteHutang(hutang) }

    fun restartAplikasi(pref: PreferenceManager) = viewModelScope.launch {
        dao.clearProduk()
        dao.clearTransaksi()
        dao.clearStruk()
        dao.clearHutang()
        dao.clearBelanja()
        pref.saveNamaToko("")
    }

}
