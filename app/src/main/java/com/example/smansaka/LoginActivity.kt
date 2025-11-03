package com.example.smansaka

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smansaka.ui.theme.SmansakaTheme
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmansakaTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = Firebase.firestore

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface) // A clean background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo/Icon
            Icon(
                imageVector = Icons.Filled.AdminPanelSettings,
                contentDescription = "School Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Admin Login",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Silakan login untuk melanjutkan",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Username Icon"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon"
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = {
                    if (username.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        db.collection("users")
                            .whereEqualTo("username", username)
                            .whereEqualTo("password", password)
                            .get()
                            .addOnSuccessListener { documents ->
                                isLoading = false
                                if (documents.isEmpty) {
                                    Toast.makeText(context, "Username atau password salah", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Login Admin Berhasil", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(context, CreateQrCodeActivity::class.java)
                                    context.startActivity(intent)
                                    (context as? ComponentActivity)?.finish()
                                }
                            }
                            .addOnFailureListener { exception ->
                                isLoading = false
                                Log.e("LoginActivity", "Gagal login ke Firestore", exception)
                                Toast.makeText(context, "Gagal login: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Username dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .shadow(6.dp, shape = RoundedCornerShape(14.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B3FE4)),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(
                        text = "LOGIN",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SmansakaTheme {
        LoginScreen()
    }
}
