package com.example.smansaka

import androidx.activity.compose.rememberLauncherForActivityResult
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smansaka.ui.theme.SmansakaTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmansakaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScanScreen()
                }
            }
        }
    }
}

@Composable
fun ScanScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var resultText by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) } // ⛔ mencegah scan berulang

    val barcodeLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            isScanning = false // buka tombol lagi setelah hasil didapat
            if (result.contents != null) {
                resultText = result.contents
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra("url", result.contents)
                context.startActivity(intent)
            } else {
                resultText = "Scan dibatalkan."
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                if (!isScanning) {
                    isScanning = true
                    val options = ScanOptions().apply {
                        setPrompt("Arahkan kamera ke barcode atau QR code")
                        setBeepEnabled(true)
                        setOrientationLocked(false)
                    }
                    barcodeLauncher.launch(options)
                }
            },
            enabled = !isScanning // tombol nonaktif selama proses scan
        ) {
            Text(if (isScanning) "Memindai..." else "Scan Barcode")
        }

        if (resultText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = resultText)
        }
    }
}
