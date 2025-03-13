package com.yuch.snapcalfirebasegemini.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot-password")
    data object Main : Screen("main")
    data object Scan : Screen("scan")
    data object Analyze : Screen("analyze/{imagePath}")
    data object Profile : Screen("profile")
    data object ManualEntry : Screen("manual-entry")
    data object Message : Screen("message")
    data object DetailFood : Screen("detail-food/{foodId}")
    data object EditFood : Screen("edit-food/{foodId}")
    data object AiChat : Screen("ai-chat")
}