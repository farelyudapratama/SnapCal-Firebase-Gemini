package com.yuch.snapcalfirebasegemini.view

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuch.snapcalfirebasegemini.ui.theme.PrimaryBlue
import com.yuch.snapcalfirebasegemini.viewmodel.AuthActionState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthActionViewModel

@Composable
fun AuthActionScreen(
    intent: Intent?, 
    onNavigateToLogin: () -> Unit,
    viewModel: AuthActionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var newPassword by remember { mutableStateOf("") }

    // 1. Eksekusi Sekali Pas Layar Dibuka
    LaunchedEffect(Unit) {
        val data = intent?.data
        if (data != null) {
            val mode = data.getQueryParameter("mode")
            val oobCode = data.getQueryParameter("oobCode")
            viewModel.handleDeepLink(mode, oobCode)
        } else {
             viewModel.handleDeepLink(null, null)
        }
    }

    // 2. Tampilan UI Berdasarkan State
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is AuthActionState.Idle, is AuthActionState.Loading -> {
                CircularProgressIndicator(color = PrimaryBlue)
            }

            is AuthActionState.SuccessVerification -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Email Berhasil Diverifikasi!", 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Masuk ke Aplikasi")
                    }
                }
            }

            is AuthActionState.ResetPasswordMode -> {
                // Form Input Password Baru
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Reset Password", 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Silakan masukkan password baru Anda.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Password Baru") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            cursorColor = PrimaryBlue
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.confirmPasswordReset(state.code, newPassword) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan Password")
                    }
                }
            }

            is AuthActionState.SuccessReset -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Password Berhasil Diubah!", 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Login Ulang")
                    }
                }
            }

            is AuthActionState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("😓", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Terjadi Kesalahan", 
                        style = MaterialTheme.typography.headlineSmall, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        state.message, 
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kembali ke Login")
                    }
                }
            }
        }
    }
}
