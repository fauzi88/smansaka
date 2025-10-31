package com.example.smansaka

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.smansaka.ui.theme.SmansakaTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

// Pastikan Anda memiliki WebViewActivity yang terdefinisi atau ganti dengan aktivitas yang sesuai.
// Jika WebViewActivity belum ada, kode ini akan menyebabkan error kompilasi pada 'Intent'.
// Untuk tujuan demonstrasi UI, anggap WebViewActivity sudah ada.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Atur kondisi agar splash screen tetap terlihat.
        // Kita akan menggunakan ini untuk menyimulasikan waktu pemuatan yang lebih lama.
        var keepSplashScreen = true
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashScreen
        }

        // Setelah penundaan, sembunyikan splash screen.
        // Di sini, kita menggunakan penundaan 2 detik. Anda bisa mengubah nilainya.
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            keepSplashScreen = false
        }, 2000L) // 2000 milidetik = 2 detik

        setContent {
            SmansakaTheme {
                // Menggunakan Surface untuk latar belakang
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen()
                }
            }
        }
    }
}

// ---

@Composable
fun DashboardScreen() {
    val context = LocalContext.current

    // Logic pemindaian dari kode awal Anda
    val barcodeLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                // Fungsionalitas: Membuka WebViewActivity dengan URL hasil scan
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra("url", result.contents)
                context.startActivity(intent)
            }
        }
    )

    fun startExamScan() {
        val options = ScanOptions().apply {
            setPrompt("Arahkan kamera ke barcode atau QR code ujian")
            setBeepEnabled(true)
            setOrientationLocked(false)
        }
        barcodeLauncher.launch(options)
    }

    Scaffold(
        topBar = {
            // TopAppBar seperti di desain gambar tidak ada, kita ganti dengan Header
            // Atau bisa dihilangkan dan ganti dengan Header Manual
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Header "Dashboard"
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Header "Selamat Datang!"
            Text(
                text = "Selamat Datang!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Sub-header
            Text(
                text = "Pilih salah satu menu di bawah ini untuk memulai.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Grid menu utama (menggunakan Column dan Row untuk tata letak seperti di gambar)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Baris 1: Scan Ujian dan Buat QR Code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Tombol 1: Scan Ujian (Fungsionalitas utama)
                    MenuCard(
                        icon = Icons.Filled.QrCodeScanner,
                        title = "Scan Ujian",
                        subtitle = "Mulai ujian dengan memindai QR code",
                        backgroundColor = Color(0xFF9C27B0).copy(alpha = 0.85f),
                        onClick = {
                            startExamScan() // Memanggil fungsionalitas scan
                        }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Tombol 2: Buat QR Code (Placeholder)
                    MenuCard(
                        icon = Icons.Filled.GridView,
                        title = "Buat QR Code",
                        subtitle = "Buat kode unik untuk ujian baru",
                        backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.85f),
                        onClick = {
                            // TODO: Tambahkan navigasi atau fungsionalitas Buat QR Code
                            // Contoh: context.startActivity(Intent(context, CreateQrCodeActivity::class.java))
                            println("Buat QR Code diklik")
                        }
                    )
                }

                // Baris 2: Hasil Ujian
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Tombol 3: Hasil Ujian (Placeholder)
                    MenuCard(
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        title = "Hasil Ujian",
                        subtitle = "Lihat daftar hasil ujian siswa",
                        backgroundColor = Color(0xFF2196F3).copy(alpha = 0.85f),
                        onClick = {
                            // TODO: Tambahkan navigasi atau fungsionalitas Hasil Ujian
                            // Contoh: context.startActivity(Intent(context, ExamResultsActivity::class.java))
                            println("Hasil Ujian diklik")
                        }
                    )

                    // Spacer untuk mengisi sisa ruang di baris (jika hanya ada 1 kartu)
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun RowScope.MenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f) // Membuat kartu mengambil proporsi yang sama di Row
            .aspectRatio(1f) // Membuat kartu berbentuk persegi
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    SmansakaTheme {
        DashboardScreen()
    }
}
