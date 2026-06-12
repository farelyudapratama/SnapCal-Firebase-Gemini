package com.yuch.snapcalfirebasegemini.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot-password")
    data object Main : Screen("main")
    data object Scan : Screen("scan")
    data object Profile : Screen("profile")
    data object ManualEntry : Screen("manual-entry")
    data object Tracking : Screen("tracking")
    data object AiChat : Screen("ai-chat")
    data object Recommendation : Screen("recommendation")
    data object Help : Screen("help")
    data object AuthAction : Screen("auth_action")

    data object Analyze : Screen("analyze/{imagePath}") {
        // Menggunakan Uri.encode() saat memanggil jika imagePath mengandung karakter khusus seperti '/'
        fun createRoute(imagePath: String) = "analyze/$imagePath"
    }
    data object DetailFood : Screen("detail-food/{foodId}") {
        fun createRoute(foodId: String) = "detail-food/$foodId"
    }
    data object EditFood : Screen("edit-food/{foodId}") {
        fun createRoute(foodId: String) = "edit-food/$foodId"
    }

    data object ProfileOnboarding : Screen("profile_onboarding?edit={edit}") {
        // Karena ini query parameter (?edit=...), kita bisa memberikan nilai default
        fun createRoute(edit: Boolean = false, section: String = ""): String {
            return "profile_onboarding?edit=$edit&section=$section"
        }
    }
}