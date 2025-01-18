package com.yuch.snapcalfirebasegemini.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Main : Screen("main")
    data object Scan : Screen("scan")
    data object Profile : Screen("profile")
}