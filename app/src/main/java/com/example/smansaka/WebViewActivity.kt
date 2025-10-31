package com.example.smansaka

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.smansaka.ui.theme.SmansakaTheme

class WebViewActivity : ComponentActivity() {
    private var pinningActivated = false
    private var showSecurityAlertDialog by mutableStateOf(false)
    private var isKeyboardVisible = false
    private var showExitDialog by mutableStateOf(false)

    // Handler untuk menunda pengecekan fokus
    private val focusHandler = Handler(Looper.getMainLooper())
    private val focusCheckRunnable = Runnable {
        // Cek kondisi hanya jika dialog belum ditampilkan
        if (!isFinishing && !showSecurityAlertDialog) {
            // Periksa ulang kondisi sesaat sebelum menampilkan dialog
            if (pinningActivated && !isKeyboardVisible && !showExitDialog && !window.decorView.hasWindowFocus()) {
                showSecurityAlertDialog = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("url") ?: "https://smansaka.sch.id"

        hideSystemBars()

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            insets
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Biarkan kosong
            }
        })

        setContent {
            SmansakaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WebPage(
                        url = url,
                        onExit = ::exitActivity,
                        showSecurityAlert = showSecurityAlertDialog,
                        onCloseSecurityDialog = { showSecurityAlertDialog = false },
                        showExitDialog = showExitDialog,
                        onShowExitDialogChange = { showExitDialog = it }
                    )
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
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onStart() {
        super.onStart()
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val isPinnedNow = am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE

        if (pinningActivated && !isPinnedNow) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }

        if (!isPinnedNow) {
            startLockTask()
        }
        pinningActivated = true
    }

    override fun onStop() {
        super.onStop()
        // Batalkan pengecekan jika aktivitas dihentikan
        focusHandler.removeCallbacks(focusCheckRunnable)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Jika fokus kembali, batalkan semua penjadwalan dialog keamanan
            focusHandler.removeCallbacks(focusCheckRunnable)
        } else {
            // Jika fokus hilang, jadwalkan pengecekan setelah jeda singkat (misalnya 150ms)
            // Ini untuk menangani kasus seperti menutup keyboard di mana fokus hilang sesaat.
             focusHandler.postDelayed(focusCheckRunnable, 300)
        }
    }
}

@Composable
fun WebPage(
    url: String,
    onExit: () -> Unit,
    showSecurityAlert: Boolean,
    onCloseSecurityDialog: () -> Unit,
    showExitDialog: Boolean,
    onShowExitDialogChange: (Boolean) -> Unit
) {
    val allowedDomains = listOf("smansaka.sch.id", "form.jotform.com", "google.com")
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url == null) return true

                    val host = url.toUri().host
                    if (host != null && allowedDomains.any { host.endsWith(it) }) {
                        return false
                    }

                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    context.startActivity(intent)
                    return true
                }
            }
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            val initialHost = url.toUri().host
            if (initialHost != null && allowedDomains.any { initialHost.endsWith(it) }) {
                loadUrl(url)
            } else {
                loadUrl("about:blank")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView({ webView }, modifier = Modifier.fillMaxSize())

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
                    containerColor = Color(0xFF154F17)
                )
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh Page")
            }
            IconButton(
                onClick = { onShowExitDialogChange(true) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFF881C27)
                )
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Keluar")
            }
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { onShowExitDialogChange(false) },
                icon = { Icon(Icons.Default.Warning, contentDescription = "Ikon Peringatan") },
                title = { Text(text = "Konfirmasi Keluar") },
                confirmButton = {
                    Button(
                        onClick = {
                            onShowExitDialogChange(false)
                            onExit()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB00020)
                        )
                    ) {
                        Text("Keluar")
                    }
                },
                dismissButton = {
                    Button(onClick = { onShowExitDialogChange(false) }) {
                        Text("Batal")
                    }
                }
            )
        }

        if (showSecurityAlert) {
            AlertDialog(
                onDismissRequest = {
                    onCloseSecurityDialog()
                    onExit()
                },
                icon = { Icon(Icons.Default.Warning, contentDescription = "Ikon Peringatan Keamanan") },
                title = { Text(text = "Peringatan Keamanan") },
                text = { Text("Aplikasi lain terdeteksi berjalan di atas aplikasi ini. Untuk alasan keamanan, aplikasi akan ditutup.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onCloseSecurityDialog()
                            onExit()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB00020)
                        )
                    ) {
                        Text("Tutup Aplikasi")
                    }
                }
            )
        }
    }
}
