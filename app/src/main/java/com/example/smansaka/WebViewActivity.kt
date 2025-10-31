package com.example.smansaka

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.smansaka.ui.theme.SmansakaTheme

class WebViewActivity : ComponentActivity() {
    private var pinningActivated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("url") ?: "https://smansaka.sch.id" // Default ke URL yang aman

        // --- SEMBUNYIKAN BILAH SISTEM ---
        hideSystemBars()

        // --- NONAKTIFKAN TOMBOL KEMBALI ---
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Biarkan kosong untuk menonaktifkan tombol kembali
            }
        })

        setContent {
            SmansakaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Teruskan URL dan fungsi keluar yang sudah diperbaiki
                    WebPage(url = url, onExit = ::exitActivity)
                }
            }
        }
    }

    private fun exitActivity() {
        stopLockTask()
        finish()
    }

    private fun hideSystemBars() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Atur agar bilah sistem muncul sementara saat digesek dari tepi
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Sembunyikan status bar dan bilah navigasi
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onStart() {
        super.onStart()


        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val isPinnedNow = am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE

        // Jika pinning sebelumnya aktif tapi sekarang tidak, berarti pengguna telah keluar.
        if (pinningActivated && !isPinnedNow) {
            // Kembali ke MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return // Keluar dari fungsi untuk mencegah pinning ulang.
        }

        // Jika tidak dipin, mulai proses pinning.
        if (!isPinnedNow) {
            startLockTask()
        }
        // Tandai bahwa pinning (seharusnya) aktif untuk pengecekan selanjutnya.
        pinningActivated = true
    }

    override fun onStop() {
        super.onStop()
        // Panggilan stopLockTask() yang lama dihapus untuk mencegah pelepasan pin
        // yang tidak disengaja (misalnya, saat ada panggilan masuk).
        // Logika di onStart() dan exitActivity() akan menangani status saat kembali ke aplikasi atau keluar.
    }
}

@Composable
fun WebPage(url: String, onExit: () -> Unit) {
    // --- PENINGKATAN KEAMANAN ---
    // Tentukan daftar domain yang diizinkan untuk dimuat di dalam WebView.
    // Ganti atau tambahkan domain lain yang Anda percayai.
    val allowedDomains = listOf("smansaka.sch.id", "form.jotform.com", "google.com")

    // State untuk menampilkan dialog konfirmasi keluar
    var showExitDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            // Konfigurasi WebViewClient untuk keamanan
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url == null) return true // Blokir jika URL null

                    val host = url.toUri().host
                    if (host != null && allowedDomains.any { host.endsWith(it) }) {
                        // Jika domain ada di dalam daftar yang diizinkan, biarkan WebView memuatnya.
                        return false
                    }

                    // Jika domain TIDAK diizinkan:
                    // 1. Blokir pemuatan di WebView dengan '''return true'''.
                    // 2. (Opsional) Buka URL tersebut di browser eksternal yang lebih aman.
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    context.startActivity(intent)
                    return true
                }
            }

            // Mengaktifkan JavaScript tetap diperlukan, tetapi kini lebih aman
            // karena URL yang dimuat sudah difilter.
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true // Diperlukan untuk beberapa web modern

            // Validasi URL awal sebelum memuatnya
            val initialHost = url.toUri().host
            if (initialHost != null && allowedDomains.any { initialHost.endsWith(it) }) {
                loadUrl(url)
            } else {
                // Jika URL awal tidak valid, muat halaman kosong untuk keamanan.
                loadUrl("about:blank")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Tampilkan WebView
        AndroidView({ webView }, modifier = Modifier.fillMaxSize())

        // Baris untuk tombol-tombol di bagian atas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { webView.reload() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFF154F17) // Hijau lembut
                )
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh Page")
            }
            IconButton(
                onClick = { showExitDialog = true },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFF881C27) // Merah lembut
                )
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Keluar")
            }
        }

        // Dialog Konfirmasi Keluar yang Ditingkatkan
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = "Ikon Peringatan") },
                title = { Text(text = "Konfirmasi Keluar") },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onExit() // Panggil fungsi keluar dari activity
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB00020) // Warna merah untuk tombol konfirmasi
                        )
                    ) {
                        Text("Ya, Keluar")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showExitDialog = false }
                    ) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}
