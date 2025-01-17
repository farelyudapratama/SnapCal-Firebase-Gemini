package com.yuch.snapcalfirebasegemini

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel

@Composable
fun SnapCalApp(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    // Daftar screen yang tidak perlu BottomBar
    val screensWithoutBottomBar = listOf(
        Screen.Login.route, // Contoh screen yang tidak membutuhkan BottomBar
        Screen.Register.route // Contoh screen lain yang tidak membutuhkan BottomBar
    )

    // Mendapatkan current route yang sedang aktif
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Menampilkan BottomBar hanya jika currentRoute tidak ada dalam daftar screensWithoutBottomBar
            if (currentRoute !in screensWithoutBottomBar) {
                BottomAppBar {
                    Text(text = "Bottom Navigation")
                }
            }
        },
        modifier = Modifier
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding.calculateBottomPadding()),
            authViewModel = authViewModel
        )
    }
}
