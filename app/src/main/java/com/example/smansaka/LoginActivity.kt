package com.example.smansaka

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smansaka.ui.theme.SmansakaTheme
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        setContent {
            SmansakaTheme {
                LoginScreen(sessionManager = sessionManager)
            }
        }
    }
}

// Sealed interface untuk merepresentasikan status UI
sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState

    fun onUsernameChange(newUsername: String) {
        username = newUsername
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun login() {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = LoginUiState.Error("Username dan password tidak boleh kosong")
            return
        }

        _loginState.value = LoginUiState.Loading

        viewModelScope.launch {
            val db = Firebase.firestore
            db.collection("users")
                .whereEqualTo("username", username.trim())
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        _loginState.value = LoginUiState.Error("Username tidak ditemukan")
                    } else {
                        val userDoc = documents.first()
                        val storedPassword = userDoc.getString("password")
                        if (storedPassword == password) {
                            _loginState.value = LoginUiState.Success
                        } else {
                            _loginState.value = LoginUiState.Error("Password salah")
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    _loginState.value = LoginUiState.Error("Gagal terhubung: ${exception.message}")
                }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginUiState.Idle
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    sessionManager: SessionManager
) {
    val context = LocalContext.current
    val loginState by loginViewModel.loginState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    // Menangani efek samping dari perubahan state login (navigasi, toast)
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginUiState.Success -> {
                sessionManager.isLoggedIn = true
                Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, AdminActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }
            is LoginUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                loginViewModel.resetLoginState() // Reset state setelah menampilkan error
            }
            else -> { /* Idle atau Loading, tidak melakukan apa-apa */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Login") },
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
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val isLoading = loginState is LoginUiState.Loading

                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = "Login Icon",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Selamat Datang, Admin",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Silakan masuk untuk melanjutkan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedTextField(
                    value = loginViewModel.username,
                    onValueChange = { loginViewModel.onUsernameChange(it) },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Username") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = loginViewModel.password,
                    onValueChange = { loginViewModel.onPasswordChange(it) },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !isLoading,
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Password") },
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, "Toggle password visibility")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { loginViewModel.login() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Login")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SmansakaTheme {
        // Inisialisasi dummy SessionManager untuk preview
        val context = LocalContext.current
        LoginScreen(sessionManager = SessionManager(context))
    }
}
