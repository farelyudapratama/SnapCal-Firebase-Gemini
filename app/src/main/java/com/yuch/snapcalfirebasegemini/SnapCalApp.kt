package com.yuch.snapcalfirebasegemini

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel

@Composable
fun SnapCalApp(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    val screensWithoutBottomBar = listOf(
        Screen.Login.route,
        Screen.Register.route
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (currentRoute !in screensWithoutBottomBar) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Semi-circular cutout effect
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-22).dp)
                                .size(64.dp)
                                .background(Color.White, CircleShape)
                        )

                        BottomNavWithFab(navController = navController)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                authViewModel = authViewModel
            )
        }

        // FAB positioned above the bottom bar cutout
        if (currentRoute !in screensWithoutBottomBar) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.Scan.route) },
                containerColor = Color(0xFFFF5722),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-40).dp)
                    .size(56.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_scan),
                    contentDescription = "Scan",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun BottomNavWithFab(
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
            modifier = modifier.height(80.dp),
            tonalElevation = 0.dp
        ) {
            // Home Item
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_home),
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

            // Empty center item for FAB space
            NavigationBarItem(
                icon = { Spacer(modifier = Modifier.width(56.dp)) },
                label = { },
                selected = false,
                onClick = { },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.White
                )
            )

            // Profile Item
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_person),
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