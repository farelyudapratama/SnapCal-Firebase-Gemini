package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.viewmodel.*

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Observasi state autentikasi
    val authState = authViewModel.authState.observeAsState()

    // Observasi email pengguna (dari AuthViewModel)
    val email by authViewModel.userEmail.observeAsState("")

    // Redirect ke login jika pengguna tidak terautentikasi
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(text = "Selamat Datang!", fontSize = 32.sp)

        // Menampilkan email pengguna
        if (email.isNotBlank()) {
            Text(text = "Login sebagai: $email", fontSize = 18.sp)
        } else {
            Text(text = "Memuat data pengguna...", fontSize = 18.sp)
        }
    }
}
