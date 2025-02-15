package com.yuch.snapcalfirebasegemini

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel

@Composable
fun SnapCalApp(
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel
) {
    val navController = rememberNavController()

    val screensWithoutBottomBar = listOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.Scan.route,
        Screen.Analyze.route,
        Screen.ManualEntry.route
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (currentRoute !in screensWithoutBottomBar) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        BottomNavbar(navController = navController)
                    }
                }
            },
            floatingActionButton = {
                if (currentRoute !in screensWithoutBottomBar) {
                    FloatingActionButton(
                        onClick = {
                            when (currentRoute) {
                                Screen.Main.route -> navController.navigate(Screen.Scan.route)
                                else -> {}
                            }
                        },
                        containerColor = Color(0xFFFF5722),
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp
                        ),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_scan),
                            contentDescription = "Scan",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                authViewModel = authViewModel,
                cameraViewModel = cameraViewModel
            )
        }
    }
}

@Composable
fun BottomNavbar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        NavigationBar(
            containerColor = Color.White,
            modifier = modifier.height(60.dp),
            tonalElevation = 0.dp
        ) {
            // Home Item
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Beranda",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "Beranda",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                },
                selected = currentRoute == Screen.Main.route,
                onClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Blue,
                    selectedTextColor = Color.Blue,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.White
                )
            )

            // Chat Item
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = "Chat",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "Chat",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                },
                selected = currentRoute == Screen.Message.route,
                onClick = {
                    navController.navigate(Screen.Message.route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Blue,
                    selectedTextColor = Color.Blue,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.White
                )
            )

            // Profile Item
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Akun",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "Akun",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                },
                selected = currentRoute == Screen.Profile.route,
                onClick = {
                    navController.navigate(Screen.Profile.route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Blue,
                    selectedTextColor = Color.Blue,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.White
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSnapCalApp() {
    // Menampilkan preview tampilan dengan parameter default (bisa ganti dengan data sebenarnya)
    SnapCalApp(authViewModel = AuthViewModel(), cameraViewModel = CameraViewModel())
}