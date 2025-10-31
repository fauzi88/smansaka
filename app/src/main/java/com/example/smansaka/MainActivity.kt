package com.example.smansaka

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var keepSplashScreen = true
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashScreen
        }

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            keepSplashScreen = false
        }, 2000L)

        setContent {
            SmansakaTheme {
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

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val qrCodePrefix = "SMANSAKA_EXAM_CODE:"

    val barcodeLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                if (result.contents.startsWith(qrCodePrefix)) {
                    val encryptedData = result.contents.removePrefix(qrCodePrefix)
                    val decryptedUrl = EncryptionUtils.decrypt(encryptedData)

                    if (decryptedUrl != null) {
                        val intent = Intent(context, WebViewActivity::class.java)
                        intent.putExtra("url", decryptedUrl)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "QR Code tidak valid atau rusak", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "QR Code tidak valid untuk aplikasi ini", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Pemindaian dibatalkan", Toast.LENGTH_SHORT).show()
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
        topBar = { }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Selamat Datang!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Pilih salah satu menu di bawah ini untuk memulai.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MenuCard(
                        icon = Icons.Filled.QrCodeScanner,
                        title = "Scan Ujian",
                        subtitle = "Mulai ujian dengan memindai QR code",
                        backgroundColor = Color(0xFF9C27B0).copy(alpha = 0.85f),
                        onClick = { startExamScan() }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    MenuCard(
                        icon = Icons.Filled.GridView,
                        title = "Buat QR Code",
                        subtitle = "Buat kode unik untuk ujian baru",
                        backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.85f),
                        onClick = { context.startActivity(Intent(context, CreateQrCodeActivity::class.java)) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MenuCard(
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        title = "Hasil Ujian",
                        subtitle = "Lihat daftar hasil ujian siswa",
                        backgroundColor = Color(0xFF2196F3).copy(alpha = 0.85f),
                        onClick = { println("Hasil Ujian diklik") }
                    )

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
            .weight(1f)
            .aspectRatio(1f)
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
