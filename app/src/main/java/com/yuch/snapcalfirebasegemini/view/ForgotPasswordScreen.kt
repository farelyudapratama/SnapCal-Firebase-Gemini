package com.yuch.snapcalfirebasegemini.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import kotlin.random.Random

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var resetEmailSent by remember { mutableStateOf(false) }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    // Pengamatan perubahan status otentikasi
    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.PasswordResetSent -> {
                // Jika reset email berhasil dikirim
                resetEmailSent = true
                Toast.makeText(context,
                    "Email reset password akan dikirim!", Toast.LENGTH_LONG).show()
                // Tidak langsung navigasi, biarkan tombol "Kembali ke Login" melakukannya
            }
            is AuthState.Error -> {
                val errorMsg = (authState.value as AuthState.Error).message

                if (errorMsg.contains("no user record", ignoreCase = true) ||
                    errorMsg.contains("user not found", ignoreCase = true) ||
                    errorMsg.contains("not registered", ignoreCase = true)) {
                    Toast.makeText(context, "Email tidak terdaftar", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
                authViewModel.resetState()
            }

            else -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF3949AB)
                    )
                )
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            repeat(15) {
                Box(
                    modifier = Modifier
                        .size((80..140).random().dp)
                        .offset(
                            x = (-30..350).random().dp,
                            y = (-30..700).random().dp
                        )
                        .background(
                            color = Color.White.copy(alpha = Random.nextFloat() * (0.08f - 0.03f) + 0.03f),
                            shape = CircleShape
                        )
                )
            }
        }

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Kembali",
                tint = Color.White
            )
        }

        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .align(Alignment.Center)
                .graphicsLayer {
                    alpha = 0.8f
                    shadowElevation = 8f
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Reset Password",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                )

                if (resetEmailSent) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Terkirim",
                            tint = Color(0xFF1A237E),
                            modifier = Modifier.size(64.dp)
                        )

                        Text(
                            text = "Periksa Email Anda",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A237E)
                        )

                        Text(
                            text = "Jika email anda terdaftar maka anda akan segera menerima intruksi reset password. Silakan periksa kotak masuk Anda.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tombol kembali ke login setelah email terkirim
                        Button(
                            onClick = {
                                // Arahkan kembali ke layar login
                                navController.navigate(Screen.Login.route) {
                                    // Hapus layar Forgot Password dari backstack
                                    popUpTo(Screen.Login.route) {
                                        inclusive = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A237E)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Text(
                                "Kembali ke Login",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Form reset password
                    Text(
                        text = "Masukkan alamat email Anda dan kami akan mengirimkan instruksi untuk mengatur ulang password Anda.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Field email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color(0xFF1A237E)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1A237E),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF1A237E),
                            cursorColor = Color(0xFF1A237E)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tombol kirim reset
                    Button(
                        onClick = {
                            if (email.isNotEmpty()) {
                                // Perbarui state otentikasi ke Loading terlebih dahulu
                                authViewModel.sendPasswordReset(email)
                            } else {
                                Toast.makeText(context, "Silakan masukkan alamat email Anda", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = authState.value != AuthState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A237E),
                            disabledContainerColor = Color(0xFF1A237E).copy(alpha = 0.5f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        if (authState.value == AuthState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Reset Password",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Opsi kembali ke login
                    Row(
                        modifier = Modifier
                            .clickable {
                                // Arahkan kembali ke layar login
                                navController.navigate(Screen.Login.route) {
                                    // Hapus layar Forgot Password dari backstack
                                    popUpTo(Screen.Login.route) {
                                        inclusive = false
                                    }
                                }
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ingat password Anda? ",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            "Login",
                            color = Color(0xFF1A237E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}