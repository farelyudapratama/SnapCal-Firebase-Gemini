package com.yuch.snapcalfirebasegemini.ui.navigation

import android.net.Uri

private const val ANALYZE_ROUTE = "analyze"
private const val ARG_ANALYZE_IMAGE_PATH = "imagePath"
private const val DETAIL_FOOD_ROUTE = "detail-food"
private const val EDIT_FOOD_ROUTE = "edit-food"
private const val ARG_FOOD_ID = "foodId"
private const val PROFILE_ONBOARDING_ROUTE = "profile_onboarding"
private const val ARG_PROFILE_ONBOARDING_EDIT = "edit"

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

    data object Analyze : Screen("$ANALYZE_ROUTE/{${ARG_ANALYZE_IMAGE_PATH}}") {
        const val ARG_IMAGE_PATH = ARG_ANALYZE_IMAGE_PATH

        fun createRoute(imagePath: String) = "$ANALYZE_ROUTE/${Uri.encode(imagePath)}"
    }

    data object DetailFood : Screen("$DETAIL_FOOD_ROUTE/{${ARG_FOOD_ID}}") {
        const val ARG_FOOD_ID = "foodId"

        fun createRoute(foodId: String) = "$DETAIL_FOOD_ROUTE/${Uri.encode(foodId)}"
    }

    data object EditFood : Screen("$EDIT_FOOD_ROUTE/{${ARG_FOOD_ID}}") {
        const val ARG_FOOD_ID = "foodId"

        fun createRoute(foodId: String) = "$EDIT_FOOD_ROUTE/${Uri.encode(foodId)}"
    }

    data object ProfileOnboarding : Screen("$PROFILE_ONBOARDING_ROUTE?${ARG_PROFILE_ONBOARDING_EDIT}={${ARG_PROFILE_ONBOARDING_EDIT}}") {
        const val ARG_EDIT = ARG_PROFILE_ONBOARDING_EDIT

        fun createRoute(edit: Boolean = false): String = "$PROFILE_ONBOARDING_ROUTE?$ARG_EDIT=$edit"
    }
}

private val bottomBarRoutes = setOf(
    Screen.Main.route,
    Screen.Tracking.route,
    Screen.Profile.route,
)

fun shouldShowBottomBar(route: String?): Boolean = route in bottomBarRoutes
