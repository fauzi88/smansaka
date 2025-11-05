package com.example.smansaka

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.smansaka.ui.theme.SmansakaTheme

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmansakaTheme {
                AdminDashboardScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Baris pertama
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminMenuCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.QrCode,
                    title = "Buat Barcode",
                    onClick = { context.startActivity(Intent(context, CreateQrCodeActivity::class.java)) }
                )
                AdminMenuCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    title = "Daftar Nilai",
                    onClick = { Toast.makeText(context, "Fitur Daftar Nilai akan datang", Toast.LENGTH_SHORT).show() }
                )
            }

            // Baris kedua
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminMenuCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Edit,
                    title = "Update Konten",
                    onClick = { Toast.makeText(context, "Fitur Update Konten akan datang", Toast.LENGTH_SHORT).show() }
                )
                AdminMenuCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Update,
                    title = "Update Jadwal",
                    onClick = { Toast.makeText(context, "Fitur Update Jadwal akan datang", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}

@Composable
fun AdminMenuCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardScreenPreview() {
    SmansakaTheme {
        AdminDashboardScreen()
    }
}
