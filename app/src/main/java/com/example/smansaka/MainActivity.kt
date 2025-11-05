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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.smansaka.ui.theme.SmansakaTheme
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
                    color = Color.White
                ) {
                    DashboardScreen()
                }
            }
        }
    }
}

class AnnouncementViewModel : ViewModel() {
    private val _announcement = MutableStateFlow("Memuat pengumuman...")
    val announcement: StateFlow<String> = _announcement

    init {
        viewModelScope.launch {
            val db = Firebase.firestore
            db.collection("pengumuman").document("penting")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        _announcement.value = "Gagal memuat pengumuman."
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        _announcement.value = snapshot.getString("teks") ?: "Tidak ada pengumuman."
                    } else {
                        _announcement.value = "Tidak ada pengumuman."
                    }
                }
        }
    }
}

@Composable
fun AnnouncementCard(announcementViewModel: AnnouncementViewModel = viewModel()) {
    val announcement by announcementViewModel.announcement.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Pengumuman",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = announcement,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, LoginActivity::class.java))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = "Admin Login"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Selamat Datang!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AnnouncementCard()

            Spacer(modifier = Modifier.height(32.dp))

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
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        title = "Jadwal Ujian",
                        subtitle = "Jadwal ujian siswa",
                        backgroundColor = Color(0xFF2196F3).copy(alpha = 0.85f),
                        onClick = { context.startActivity(Intent(context, JadwalActivity::class.java)) }
                    )
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
