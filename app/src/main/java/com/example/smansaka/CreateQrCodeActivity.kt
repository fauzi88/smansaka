package com.example.smansaka

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smansaka.ui.theme.SmansakaTheme
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class CreateQrCodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmansakaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CreateQrCodeScreen { finish() }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQrCodeScreen(onNavigateUp: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val qrCodePrefix = "SMANSAKA_EXAM_CODE:"
    val context = LocalContext.current

    val generateQrCode = {
        if (text.isBlank()) {
            Toast.makeText(context, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show()
        } else {
            val encryptedContent = EncryptionUtils.encrypt(text)
            if (encryptedContent != null) {
                val contentToEncode = qrCodePrefix + encryptedContent
                try {
                    val barcodeEncoder = BarcodeEncoder()
                    qrCodeBitmap = barcodeEncoder.encodeBitmap(contentToEncode, BarcodeFormat.QR_CODE, 512, 512)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Gagal membuat QR Code", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Gagal mengenkripsi data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val saveQrCode = {
        val bitmapToSave = qrCodeBitmap
        if (bitmapToSave != null) {
            saveBitmapToGallery(context, bitmapToSave, "QRCode_${System.currentTimeMillis()}")
        }
    }

    val createAnother = {
        qrCodeBitmap = null
        text = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat QR Code") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (qrCodeBitmap == null) {
                // State 1: Input and Generate
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Masukkan URL atau data") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = generateQrCode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Buat QR Code")
                }
            } else {
                // State 2: Display and Save
                qrCodeBitmap?.let { bitmap ->
                    Text(
                        "QR Code Berhasil Dibuat",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(280.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = saveQrCode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Simpan ke Galeri")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = createAnother,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Buat Lagi")
                    }
                }
            }
        }
    }
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, displayName: String) {
    val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(imageCollection, contentValues)

    uri?.let {
        try {
            resolver.openOutputStream(it)?.use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                    throw Exception("Gagal menyimpan bitmap.")
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }

            Toast.makeText(context, "QR Code berhasil disimpan", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            Toast.makeText(context, "Gagal menyimpan QR Code: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    } ?: run {
        Toast.makeText(context, "Gagal membuat file media", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun CreateQrCodeScreenPreview() {
    SmansakaTheme {
        CreateQrCodeScreen(onNavigateUp = {})
    }
}
