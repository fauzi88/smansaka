package com.example.smansaka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smansaka.ui.theme.SmansakaTheme
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// 1. DATA CLASS DIPERBARUI: Menambahkan field 'kelas'
data class JadwalItem(
    val kelas: String = "",
    val mapel: String = "",
    val tanggal: String = "",
    val jam: String = ""
)

// 2. VIEWMODEL DIPERBARUI: Mengelompokkan data jadwal
class JadwalViewModel : ViewModel() {
    private val _groupedJadwal = MutableStateFlow<Map<String, Map<String, List<JadwalItem>>>>(emptyMap())
    val groupedJadwal: StateFlow<Map<String, Map<String, List<JadwalItem>>>> = _groupedJadwal

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchJadwal()
    }

    private fun fetchJadwal() {
        viewModelScope.launch {
            _isLoading.value = true
            val db = Firebase.firestore
            db.collection("jadwal")
                // .orderBy("kelas") // Mengurutkan berdasarkan kelas
                // .orderBy("tanggal") // Lalu berdasarkan tanggal
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val jadwalItems = snapshot.toObjects(JadwalItem::class.java)
                        // Mengelompokkan data berdasarkan 'kelas', lalu 'tanggal'
                        val groupedData = jadwalItems.groupBy { it.kelas }
                            .mapValues { entry ->
                                entry.value.groupBy { it.tanggal }
                            }
                        _groupedJadwal.value = groupedData
                    }
                    _isLoading.value = false
                }
        }
    }
}

class JadwalActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmansakaTheme {
                JadwalScreen()
            }
        }
    }
}

// --- Helper untuk format tanggal ---
fun formatDisplayDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString // Fallback jika format salah
    }
}

// 3. TAMPILAN UTAMA DIPERBARUI
@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalScreen(jadwalViewModel: JadwalViewModel = viewModel()) {
    val groupedJadwal by jadwalViewModel.groupedJadwal.collectAsState()
    val isLoading by jadwalViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jadwal Ujian") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = Color(0xFFF0F2F5)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (groupedJadwal.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Saat ini belum ada jadwal ujian yang dipublikasikan.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Loop untuk setiap KELAS
                    groupedJadwal.toSortedMap().forEach { (kelas, jadwalPerTanggal) ->
                        // Header Kelas yang "lengket"
                        stickyHeader(key = "header-$kelas") {
                            KelasHeader(kelas)
                        }
                        // Padding untuk konten di bawah header
                        item(key = "spacer-after-header-$kelas") { Spacer(modifier = Modifier.height(16.dp)) }

                        // PERBAIKAN: Menggunakan `items` untuk daftar tanggal
                        items(
                            items = jadwalPerTanggal.toSortedMap().entries.toList(),
                            key = { "${kelas}-${it.key}" } // Gunakan kombinasi kelas dan tanggal sebagai key unik
                        ) { (tanggal, daftarMapel) ->
                            JadwalTanggalCard(tanggal = tanggal, daftarMapel = daftarMapel)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KelasHeader(kelas: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F2F5))
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Text(
            text = kelas,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun JadwalTanggalCard(tanggal: String, daftarMapel: List<JadwalItem>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = formatDisplayDate(tanggal),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Daftar Mata Pelajaran
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                daftarMapel.forEach { jadwal ->
                    Text(
                        text = "• ${jadwal.mapel}: ${jadwal.jam}",
                        fontSize = 15.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}
