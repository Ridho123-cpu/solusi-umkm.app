package com.salman.umkm

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.salman.umkm.ui.theme.UMKMTheme
import java.text.NumberFormat
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID


// Enum untuk Navigasi
enum class Screen { Beranda, Kasir, Stok, Belanja, Hutang, Bantuan }

// Fungsi Helper Format Rupiah
fun formatRupiah(number: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(number).replace("Rp", "Rp ").replace(",00", "")
}

fun formatTanggal(millis: Long): String {
    val formatter = SimpleDateFormat("EEEE, dd MMM yyyy - HH:mm", Locale("id", "ID"))
    return formatter.format(Date(millis))
}

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color(0xFF0D47A1),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "SOLUSI UMKM",
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = Color(0xFF0D47A1)
        )
        Text(
            text = "Digitalisasi Ekonomi Indonesia",
            fontSize = 12.sp,
            color = Color.DarkGray
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefManager = PreferenceManager(this)
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.produkDao()
        val viewModel = MainViewModel(dao)


        enableEdgeToEdge()
        setContent {
            UMKMTheme {
                val prefManager = remember { PreferenceManager(applicationContext) }
                var namaToko by remember { mutableStateOf(prefManager.getNamaToko()) }

                if (namaToko.isNullOrBlank()) {
                    OnboardingScreen { namaBaru ->
                        prefManager.saveNamaToko(namaBaru)
                        namaToko = namaBaru
                    }
                } else {
                    MainApp(
                        viewModel = viewModel,
                        namaToko = namaToko!!,
                        onLogout = {
                            namaToko = null // Ini akan memicu navigasi kembali ke Onboarding
                        },
                        onShare = { trx -> shareToWhatsApp(namaToko!!, trx.strukId, listOf(trx)) }
                    )
                }
            }
        }
    }

    private fun shareToWhatsApp(namaToko: String, idStruk: String, listBarang: List<Transaksi>) {
        val totalBayar = listBarang.sumOf { it.totalHarga }
        val detailBarang = listBarang.joinToString("\n") {
            "- ${it.namaProduk} (${it.jumlah}x): ${formatRupiah(it.totalHarga)}"
        }

        val pesan = """
        *STRUK DIGITAL $namaToko*
        --------------------------------------
        ID: #$idStruk
        Tanggal: ${formatTanggal(System.currentTimeMillis())}
        
        *Rincian Belanja:*
        $detailBarang
        
        *Total Akhir: ${formatRupiah(totalBayar)}*
        --------------------------------------
        Terima kasih telah berbelanja!
    """.trimIndent()

        try {
            val sendIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(pesan))
            }
            startActivity(sendIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp tidak terpasang", Toast.LENGTH_SHORT).show()
        }
    }
    private fun hubungiBantuan(context: android.content.Context) {
        val nomorWA = "6283147336016" // (Gunakan kode negara 62)
        val pesan = "Halo Admin SOLUSI UMKM, saya butuh bantuan mengenai aplikasi..."
        val url = "https://api.whatsapp.com/send?phone=$nomorWA&text=${java.net.URLEncoder.encode(pesan, "UTF-8")}"

        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse(url)
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "WhatsApp tidak terpasang", android.widget.Toast.LENGTH_SHORT).show()
        }
    }


    @Composable
    fun OnboardingScreen(onFinish: (String) -> Unit) {
        var textInput by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AppLogo(modifier = Modifier.padding(bottom = 48.dp))

            Text(
                "Selamat Datang!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0D47A1)
            )
            Text(
                "Mulai kelola bisnis Anda secara digital hari ini.",
                textAlign = TextAlign.Center,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Nama Toko/Usaha Anda") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFF0D47A1),
                    focusedLabelColor = Color(0xFF0D47A1),
                    cursorColor = Color(0xFF0D47A1)
                ),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { if (textInput.isNotEmpty()) onFinish(textInput) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Mulai Bisnis", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainApp(
        viewModel: MainViewModel,
        namaToko: String,
        onLogout: () -> Unit,
        onShare: (Transaksi) -> Unit
    ) {
        var currentScreen by remember { mutableStateOf(Screen.Beranda) }
        var showMenu by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val prefManager = PreferenceManager(context)
        var showRestartDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                if (currentScreen != Screen.Bantuan) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(35.dp),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = namaToko.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(namaToko, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(currentScreen.name, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Bantuan") },
                                    onClick = {
                                        showMenu = false
                                        currentScreen = Screen.Bantuan
                                    },
                                    leadingIcon = { Icon(Icons.Default.Help, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Hapus Bisnis", color = Color.Red) },
                                    onClick = {
                                        showMenu = false
                                        showRestartDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.Logout, null, tint = Color.Red) }
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D47A1))
                    )
                }
            },
            bottomBar = {
                if (currentScreen != Screen.Bantuan) {
                    MyBottomNavigation(currentScreen) { currentScreen = it }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentScreen) {
                    Screen.Beranda -> BerandaScreen(
                        viewModel = viewModel,
                        namaToko = namaToko,
                        onShareClick = { transaksi ->
                            shareToWhatsApp(namaToko, transaksi.strukId, listOf(transaksi))
                        },
                        onNavigateToKasir = { namaProduk -> currentScreen = Screen.Kasir })

                    Screen.Kasir -> KasirScreen(viewModel)
                    Screen.Stok -> StokScreen(viewModel)
                    Screen.Belanja -> BelanjaScreen(viewModel)
                    Screen.Hutang -> HutangScreen(viewModel)
                    Screen.Bantuan -> BantuanScreen(onBack = { currentScreen = Screen.Beranda })
                }
            }
        }
        if (showRestartDialog) {
            AlertDialog(
                onDismissRequest = { showRestartDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                        Spacer(Modifier.width(8.dp))
                        Text("Peringatan Penting!", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text("Apakah Anda yakin ingin memulai ulang usaha? \n\nSemua data (Produk, Stok, Riwayat Penjualan, dan Hutang) akan **DIHAPUS PERMANEN** dan tidak bisa dikembalikan.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showRestartDialog = false
                            viewModel.restartAplikasi(prefManager)
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Ya, Hapus Bisnis ini")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestartDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }


    @Composable
    fun BerandaScreen(
        viewModel: MainViewModel,
        namaToko: String,
        onShareClick: (Transaksi) -> Unit,
        onNavigateToKasir: (String) -> Unit
    ) {
        val totalOmzet by viewModel.totalPendapatan.collectAsState(initial = 0.0)
        val totalLaba by viewModel.totalLaba.collectAsState(initial = 0.0)
        val scope = rememberCoroutineScope()
        val daftarStruk by viewModel.daftarStruk.collectAsState(initial = emptyList())
        val terlaris by viewModel.produkTerlaris.collectAsState()

        var detailStruk by remember { mutableStateOf<List<Transaksi>?>(null) }
        var idStrukTerpilih by remember { mutableStateOf("") }
        var searchRiwayatQuery by remember { mutableStateOf("") }
        var isVisible by remember { mutableStateOf(true) }

        val filteredStruk = daftarStruk.filter { struk ->
            val tanggalStr = formatTanggal(struk.tanggal) // Menggunakan fungsi formatTanggal kita
            tanggalStr.contains(searchRiwayatQuery, ignoreCase = true)
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // 1. Header Row (Judul Halaman + Tombol Hide)
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            tint = Color(0xFF0D47A1),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Ringkasan Bisnis",
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = Color(0xFF0D47A1)
                        )
                    }

                    // TOMBOL HIDE/SHOW SALDO
                    IconButton(onClick = { isVisible = !isVisible }) {
                        Icon(
                            imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFF0D47A1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Card Omzet & Laba
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D47A1)),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            "Total Omzet (Uang Masuk)",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        // LOGIKA HIDE OMZET
                        Text(
                            text = if (isVisible) formatRupiah(
                                totalOmzet ?: 0.0
                            ) else "Rp ••••••••",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Spacer(Modifier.height(20.dp))

                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Laba Bersih (Keuntungan)",
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )

                                    // LOGIKA HIDE LABA
                                    Text(
                                        text = if (isVisible) formatRupiah(
                                            totalLaba ?: 0.0
                                        ) else "Rp ••••••••",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF00E676)
                                    )
                                }
                            }
                        }
                    }
                }
                if (terlaris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Produk Terlaris 🔥",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp)
                    ) {
                        terlaris.forEach { nama ->
                            SuggestionChip(
                                onClick = { onNavigateToKasir(nama) },
                                label = { Text(nama) },
                                modifier = Modifier.padding(end = 8.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    labelColor = Color(0xFF0D47A1)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- FITUR PENCARIAN RIWAYAT ---
                Text("Riwayat Penjualan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                OutlinedTextField(
                    value = searchRiwayatQuery,
                    onValueChange = { searchRiwayatQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text("Cari waktu (Senin, 12 Jan, 08:00...)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
            }

            // Daftar Riwayat yang sudah di-filter
                if (filteredStruk.isEmpty()) {
                    item {
                        Text(
                            "Riwayat tidak ditemukan",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                } else {
                    items(filteredStruk) { struk ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    idStrukTerpilih = struk.id
                                    scope.launch {
                                        detailStruk = viewModel.getDetailTransaksi(struk.id)
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFFF0F0F0)
                            )
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                Arrangement.SpaceBetween,
                                Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("ID: ${struk.id}", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Text(
                                        formatTanggal(struk.tanggal),
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        if (isVisible) formatRupiah(struk.totalBelanja) else "Rp ••",
                                        fontWeight = FontWeight.Black, color = Color(0xFF0D47A1)
                                    )
                                }
                                IconButton(onClick = { viewModel.hapusTransaksi(struk.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        tint = Color.Red.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }


        // 4. Dialog Detail Struk yang menampilkan banyak barang
        detailStruk?.let { listBarang ->
            AlertDialog(
                onDismissRequest = { detailStruk = null },
                title = { Text("Detail Struk", fontWeight = FontWeight.Bold) },
                text = {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "ID: #$idStrukTerpilih",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))

                        // List barang di dalam struk
                        listBarang.forEach { item ->
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text(item.namaProduk, Modifier.weight(1f))
                                Text("${item.jumlah} x ${formatRupiah(item.totalHarga / item.jumlah)}")
                            }
                        }

                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        val total = listBarang.sumOf { it.totalHarga }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.Bold)
                            Text(
                                formatRupiah(total),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D47A1)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (detailStruk != null) {
                                shareToWhatsApp(namaToko, idStrukTerpilih, detailStruk!!)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Text("Bagikan ke WhatsApp")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { detailStruk = null }) { Text("Tutup") }
                }
            )
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun KasirScreen(viewModel: MainViewModel) {
        val daftarProduk by viewModel.daftarProduk.collectAsState(initial = emptyList())
        var searchQuery by remember { mutableStateOf("") }

        // Keranjang belanja sementara
        var keranjang by remember { mutableStateOf(listOf<Pair<Produk, Double>>()) }
        var produkSedangDipilih by remember { mutableStateOf<Produk?>(null) }

        val filteredProduk = daftarProduk.filter {
            it.nama.contains(searchQuery, ignoreCase = true)
        }

        Scaffold(
            bottomBar = {
                if (keranjang.isNotEmpty()) {var showHutangDialog by remember { mutableStateOf(false) }
                    var namaPelangganHutang by remember { mutableStateOf("") }
                    val totalBayar = keranjang.sumOf { it.first.hargaJual * it.second }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D47A1)),
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Total: ${formatRupiah(totalBayar)}", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Tombol Bayar Tunai
                                Button(
                                    onClick = {
                                        viewModel.prosesPenjualan(
                                            keranjang = keranjang,
                                            sebagaiHutang = false
                                        )
                                        keranjang = emptyList()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Text("BAYAR TUNAI", color = Color(0xFF0D47A1), fontSize = 12.sp)
                                }

                                // Tombol Catat Hutang
                                OutlinedButton(
                                    onClick = { showHutangDialog = true },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, Color.White),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text("HUTANG", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Dialog Input Nama Pelanggan Hutang
                    if (showHutangDialog) {
                        AlertDialog(
                            onDismissRequest = { showHutangDialog = false },
                            title = { Text("Catat Sebagai Hutang") },
                            text = {
                                Column {
                                    Text("Total: ${formatRupiah(totalBayar)}")
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = namaPelangganHutang,
                                        onValueChange = { namaPelangganHutang = it },
                                        label = { Text("Nama Pelanggan") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    if (namaPelangganHutang.isNotEmpty()) {
                                        // Memanggil fungsi baru dengan parameter sebagaiHutang = true
                                        viewModel.prosesPenjualan(
                                            keranjang = keranjang,
                                            sebagaiHutang = true,
                                            namaPembeli = namaPelangganHutang
                                        )

                                        keranjang = emptyList()
                                        showHutangDialog = false
                                        namaPelangganHutang = ""
                                    }
                                }) { Text("Catat Hutang") }
                            },
                            dismissButton = { TextButton(onClick = { showHutangDialog = false }) { Text("Batal") } }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text("Kasir Digital", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text("Cari barang...") },
                    leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    shape = MaterialTheme.shapes.medium
                )

                // List Produk
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredProduk) { produk ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { produkSedangDipilih = produk }
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(produk.nama, fontWeight = FontWeight.Bold)
                                    Text("Stok: ${produk.stok} ${produk.satuan}", fontSize = 12.sp)
                                }
                                Text(formatRupiah(produk.hargaJual), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Tampilan Keranjang (Preview Singkat)
                if (keranjang.isNotEmpty()) {
                    Text(
                        "Keranjang Belanja:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 120.dp)
                            .padding(vertical = 8.dp)
                    ) {
                        items(keranjang) { item ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${item.first.nama} (${item.second} ${item.first.satuan})",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0D47A1),
                                    fontSize = 14.sp
                                )
                                IconButton(onClick = {
                                    keranjang = keranjang.filter { it != item }
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        Modifier.size(20.dp),
                                        tint = Color.Red.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                        }
                    }
                }
            }
        }

        // Dialog Input Jumlah
        produkSedangDipilih?.let { produk ->
            JualProdukDialog(
                produk = produk,
                onDismiss = { produkSedangDipilih = null },
                onConfirm = { jml ->
                    // Masukkan ke keranjang, bukan langsung ke database
                    keranjang = keranjang + (produk to jml)
                    produkSedangDipilih = null
                }
            )
        }
    }


    @Composable
    fun StokScreen(viewModel: MainViewModel) {
        val daftarProduk by viewModel.daftarProduk.collectAsState(initial = emptyList())
        var showAddDialog by remember { mutableStateOf(false) }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Manajemen Inventori", fontWeight = FontWeight.Bold)
                LazyColumn {
                    items(daftarProduk) { produk ->
                        ListItem(
                            headlineContent = { Text(produk.nama) },
                            supportingContent = {
                                Text(
                                    text = "${formatRupiah(produk.hargaJual)} / ${produk.satuan}",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.hapusProduk(produk) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color.Red
                                    )
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        if (showAddDialog) {
            TambahProdukDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { nama, hBeli, hJual, stok, satuan ->
                    viewModel.tambahProduk(nama, hBeli, hJual, stok, satuan)
                    showAddDialog = false
                }
            )
        }
    }

    @Composable
    fun BelanjaScreen(viewModel: MainViewModel) {
        var itemBaru by remember { mutableStateOf("") }
        val daftarBelanja by viewModel.daftarBelanja.collectAsState(initial = emptyList())

        Column(Modifier.padding(16.dp)) {
            Text("Daftar Belanja Stok", fontWeight = FontWeight.Bold, fontSize = 20.sp)

            Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = itemBaru,
                    onValueChange = { itemBaru = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Tambah Kebutuhan Toko") },
                    singleLine = true
                )
                IconButton(onClick = {
                    if (itemBaru.isNotEmpty()) {
                        viewModel.tambahBelanja(itemBaru)
                        itemBaru = ""
                    }
                }) { Icon(Icons.Default.Add, contentDescription = null) }
            }

            LazyColumn {
                items(daftarBelanja) { belanja ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = belanja.isChecked,
                            onCheckedChange = { viewModel.updateStatusBelanja(belanja, it) }
                        )
                        Text(
                            text = belanja.item,
                            modifier = Modifier.weight(1f),
                            style = if (belanja.isChecked)
                                androidx.compose.ui.text.TextStyle(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                                    color = Color.Gray
                                )
                            else androidx.compose.ui.text.TextStyle.Default
                        )
                        IconButton(onClick = { viewModel.hapusBelanja(belanja) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.LightGray
                            )
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }

            if (daftarBelanja.any { it.isChecked }) {
                Button(
                    onClick = { viewModel.hapusYangSelesai() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Hapus Barang Terbeli")
                }
            }
        }
    }

    @Composable
    fun HutangScreen(viewModel: MainViewModel) {
        val daftarHutang by viewModel.daftarHutang.collectAsState(initial = emptyList())
        var showAdd by remember { mutableStateOf(false) }
        var hutangSedangDicicil by remember { mutableStateOf<Hutang?>(null) }

        // Definisikan variabel input di sini
        var namaHutangInput by remember { mutableStateOf("") }
        var jumlahHutangInput by remember { mutableStateOf("") }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAdd = true },
                    containerColor = Color(0xFF0D47A1)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }
        ) { p ->
            Column(Modifier
                .padding(p)
                .padding(16.dp)) {
                Text("Catatan Hutang", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                LazyColumn {
                    items(daftarHutang) { h ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (h.status != "LUNAS") {
                                    Checkbox(
                                        checked = false, // Selalu false karena jika true akan langsung diproses lunas
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) {
                                                // Jika dicentang, langsung bayar sebesar sisa hutang
                                                viewModel.cicilHutang(h, h.sisaHutang)
                                            }
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(h.namaPelanggan, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Sisa: ${formatRupiah(h.sisaHutang)}",
                                        color = if (h.status == "LUNAS") Color.Gray else Color.Red
                                    )
                                    if (h.status == "LUNAS") {
                                        Text(
                                            "Status: LUNAS ✅",
                                            fontSize = 11.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }

                                // Tombol Aksi
                                if (h.status != "LUNAS") {
                                    IconButton(onClick = { hutangSedangDicicil = h }) {
                                        Icon(Icons.Default.Edit, "Cicil", tint = Color.Blue)
                                    }
                                } else {
                                    // Tombol Hapus muncul jika sudah LUNAS
                                    IconButton(onClick = { viewModel.hapusHutang(h) }) {
                                        Icon(Icons.Default.Delete, "Hapus", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (hutangSedangDicicil != null) {
                val hutang = hutangSedangDicicil!!
                var nominalCicil by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { hutangSedangDicicil = null },
                    title = { Text("Bayar Cicilan: ${hutang.namaPelanggan}") },
                    text = {
                        Column {
                            Text("Sisa Hutang: ${formatRupiah(hutang.sisaHutang)}")
                            OutlinedTextField(
                                value = nominalCicil,
                                onValueChange = { if (it.all { c -> c.isDigit() }) nominalCicil = it },
                                label = { Text("Nominal Bayar") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            val nominal = nominalCicil.toDoubleOrNull() ?: 0.0
                            viewModel.cicilHutang(hutang, nominal)
                            hutangSedangDicicil = null
                        }) { Text("Update") }
                    },
                    dismissButton = {
                        TextButton(onClick = { hutangSedangDicicil = null }) { Text("Batal") }
                    }
                )
            }

            if (showAdd) {
                AlertDialog(
                    onDismissRequest = { showAdd = false },
                    title = { Text("Tambah Hutang Baru") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = namaHutangInput,
                                onValueChange = { namaHutangInput = it },
                                label = { Text("Nama Pelanggan") }
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = jumlahHutangInput,
                                onValueChange = { if (it.all { c -> c.isDigit() }) jumlahHutangInput = it },
                                label = { Text("Total Hutang") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.tambahHutang(namaHutangInput, jumlahHutangInput.toDoubleOrNull() ?: 0.0)
                            namaHutangInput = ""
                            jumlahHutangInput = ""
                            showAdd = false
                        }) { Text("Simpan") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAdd = false }) { Text("Batal") }
                    }
                )
            }
        }
    }

    @Composable
    fun MyBottomNavigation(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
        NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
            val items = listOf(
                Triple(Screen.Beranda, "Beranda", Icons.Default.Home),
                Triple(Screen.Kasir, "Kasir", Icons.Default.ShoppingCart),
                Triple(Screen.Hutang, "Hutang", Icons.Default.HistoryEdu),
                Triple(Screen.Stok, "Stok", Icons.Default.Inventory),
                Triple(Screen.Belanja, "Belanja", Icons.Default.FormatListBulleted)
            )
            items.forEach { (screen, label, icon) ->
                NavigationBarItem(
                    selected = currentScreen == screen,
                    onClick = { onScreenSelected(screen) },
                    label = { Text(label, fontSize = 10.sp) },
                    icon = { Icon(icon, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0D47A1),
                        selectedTextColor = Color(0xFF0D47A1),
                        indicatorColor = Color(0xFFE3F2FD)
                    )
                )
            }
        }
    }
    @Composable
    fun JualProdukDialog(
        produk: Produk,
        onDismiss: () -> Unit,
        onConfirm: (Double) -> Unit
    ) {
        var jumlah by remember { mutableStateOf("1") }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Jual ${produk.nama}") },
            text = {
                Column {
                    Text("Harga Satuan: ${formatRupiah(produk.hargaJual)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jumlah,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null || it == ".") jumlah = it },
                        label = { Text("Jumlah Jual (${produk.satuan})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val jml = jumlah.toDoubleOrNull() ?: 0.0
                Button(
                    onClick = { onConfirm(jml) },
                    enabled = jml > 0 && jml <= produk.stok
                ) {
                    Text(if (jml > produk.stok) "Stok Kurang" else "Konfirmasi")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Batal") }
            }
        )
    }
    @Composable
    fun TambahProdukDialog(
        onDismiss: () -> Unit,
        onConfirm: (String, Double, Double, Double, String) -> Unit
    ) {
        var nama by remember { mutableStateOf("") }
        var hargaBeli by remember { mutableStateOf("") }
        var hargaJual by remember { mutableStateOf("") }
        var stok by remember { mutableStateOf("") }
        var satuan by remember { mutableStateOf("Kg") }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Tambah Produk Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Barang") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stok,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null || it == ".") stok = it },
                            label = { Text("Stok") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(value = satuan, onValueChange = { satuan = it }, label = { Text("Satuan") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(
                        value = hargaBeli, onValueChange = { if (it.all { c -> c.isDigit() }) hargaBeli = it },
                        label = { Text("Harga Modal / $satuan") }, prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hargaJual, onValueChange = { if (it.all { c -> c.isDigit() }) hargaJual = it },
                        label = { Text("Harga Jual / $satuan") }, prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onConfirm(nama, hargaBeli.toDoubleOrNull() ?: 0.0, hargaJual.toDoubleOrNull() ?: 0.0, stok.toDoubleOrNull() ?: 0.0, satuan)
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
        )
    }
    @Composable
    fun BantuanScreen(onBack: () -> Unit) {
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color(0xFF0D47A1)
                    )
                }
                Text(
                    "Pusat Bantuan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Panduan Penggunaan", fontWeight = FontWeight.Bold, color = Color.Black)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(Modifier.padding(16.dp)) {
                    val panduan = listOf(
                        "🏠 Beranda: Pantau uang masuk dan keuntungan Anda.",
                        "🛒 Kasir: Gunakan sistem keranjang untuk banyak barang.",
                        "📦 Stok: Atur modal, harga jual, dan satuan (Kg/Pcs).",
                        "💸 Hutang: Catat cicilan atau pelunasan pelanggan.",
                        "📝 Belanja: Daftar otomatis jika stok barang habis."
                    )
                    panduan.forEach { teks ->
                        Text(teks, fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Memanggil fungsi bantuan via WhatsApp
                    val nomorWA = "6283147336016"
                    val pesan = "Halo Admin SOLUSI UMKM, saya butuh bantuan mengenai aplikasi..."
                    val url = "https://api.whatsapp.com/send?phone=$nomorWA&text=${java.net.URLEncoder.encode(pesan, "UTF-8")}"

                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                        intent.data = android.net.Uri.parse(url)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "WhatsApp tidak terpasang", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(Icons.Default.Call, null)
                Spacer(Modifier.width(8.dp))
                Text("Hubungi Admin via WhatsApp")
            }
        }
    }
} // Penutup Class MainActivity








