package com.yuch.snapcalfirebasegemini

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.view.AiChatScreen
import com.yuch.snapcalfirebasegemini.view.AnalyzeScreen
import com.yuch.snapcalfirebasegemini.view.NutriTrackScreen
import com.yuch.snapcalfirebasegemini.view.DetailFoodScreen
import com.yuch.snapcalfirebasegemini.view.EditFoodScreen
import com.yuch.snapcalfirebasegemini.view.ForgotPasswordScreen
import com.yuch.snapcalfirebasegemini.view.LoginScreen
import com.yuch.snapcalfirebasegemini.view.MainScreen
import com.yuch.snapcalfirebasegemini.view.ManualEntryScreen
import com.yuch.snapcalfirebasegemini.view.ProfileOnboardingScreen
import com.yuch.snapcalfirebasegemini.view.ProfileScreen
import com.yuch.snapcalfirebasegemini.view.RegisterScreen
import com.yuch.snapcalfirebasegemini.view.camera.ScanScreen
import com.yuch.snapcalfirebasegemini.viewmodel.AiChatViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.OnboardingViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    onboardingViewModel: OnboardingViewModel,
    cameraViewModel: CameraViewModel,
    foodViewModel: FoodViewModel,
    aiChatViewModel: AiChatViewModel
) {
    val authState = authViewModel.authState.observeAsState()

    val startDestination = when (authState.value) {
        is AuthState.Authenticated -> Screen.Main.route
        else -> Screen.Login.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(modifier, navController, authViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(modifier, navController, authViewModel)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(modifier, navController, authViewModel)
        }
        composable(Screen.Main.route) {
            MainScreen(modifier, navController, authViewModel, foodViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(modifier, navController, authViewModel, profileViewModel)
        }
        composable(Screen.Tracking.route) {
            NutriTrackScreen(navController, authViewModel, foodViewModel)
        }
        composable(
            Screen.DetailFood.route,
            arguments = listOf(
                navArgument("foodId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) {
            val foodId = it.arguments?.getString("foodId")
            requireNotNull(foodId) { "FoodId cannot be null" }
            DetailFoodScreen(
                foodId = foodId,
                onBack = { navController.popBackStack() },
                viewModel = foodViewModel,
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(Screen.Scan.route) {
            ScanScreen(
                modifier, navController, authViewModel, viewModel = cameraViewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            Screen.Analyze.route,
            arguments = listOf(
                navArgument("imagePath") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { entry ->
            val imagePath = entry.arguments?.getString("imagePath")
            requireNotNull(imagePath) { "Image path cannot be null" }

            AnalyzeScreen(
                imagePath = imagePath,
                onBack = { navController.popBackStack() },
                viewModel = foodViewModel,
                onSuccessfulUpload = {
                    navController.popBackStack(
                        route = Screen.Main.route,
                        inclusive = false
                    )
                }
            )
        }
        composable(Screen.ManualEntry.route) {
            ManualEntryScreen(
                modifier,
                onBack = { navController.popBackStack() },
                viewModel = foodViewModel,
                onSuccessfulUpload = {
                    navController.popBackStack(
                        route = Screen.Main.route,
                        inclusive = false
                    )
                }
            )
        }
        composable(
            route = Screen.EditFood.route,
            arguments = listOf(
                navArgument("foodId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId")!!
            val foodItem by foodViewModel.food.collectAsStateWithLifecycle()

            EditFoodScreen(
                foodId = foodId,
                navController = navController,
                foodItem = foodItem,
                onUpdateFood = { id, imagePath, foodData ->
                    foodViewModel.updateFood(id, imagePath, foodData)
                },
                onBack = { navController.popBackStack() },
                foodViewModel = foodViewModel
            )
        }

        composable(Screen.AiChat.route) {
            AiChatScreen(
                aiChatViewModel = aiChatViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("profile_onboarding") {
            ProfileOnboardingScreen(
                navController = navController,
                authViewModel = authViewModel,
                profileViewModel = profileViewModel,
                onboardingViewModel = onboardingViewModel,
            )
        }
    }
}
