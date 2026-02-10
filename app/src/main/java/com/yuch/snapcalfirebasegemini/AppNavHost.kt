package com.yuch.snapcalfirebasegemini

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.view.AiChatScreen
import com.yuch.snapcalfirebasegemini.view.AnalyzeScreen
import com.yuch.snapcalfirebasegemini.view.NutriTrackScreen
import com.yuch.snapcalfirebasegemini.view.DetailFoodScreen
import com.yuch.snapcalfirebasegemini.view.EditFoodScreen
import com.yuch.snapcalfirebasegemini.view.ForgotPasswordScreen
import com.yuch.snapcalfirebasegemini.view.HelpScreen
import com.yuch.snapcalfirebasegemini.view.LoginScreen
import com.yuch.snapcalfirebasegemini.view.MainScreen
import com.yuch.snapcalfirebasegemini.view.ManualEntryScreen
import com.yuch.snapcalfirebasegemini.view.ProfileOnboardingScreen
import com.yuch.snapcalfirebasegemini.view.ProfileScreen
import com.yuch.snapcalfirebasegemini.view.RecommendationScreen
import com.yuch.snapcalfirebasegemini.view.RegisterScreen
import com.yuch.snapcalfirebasegemini.view.camera.ScanScreen
import com.yuch.snapcalfirebasegemini.viewmodel.AiChatViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.AnnouncementViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.OnboardingViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.RecommendationViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ViewModelFactory

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    onboardingViewModel: OnboardingViewModel,
    cameraViewModel: CameraViewModel,
    getFoodViewModel: GetFoodViewModel,
    announcementViewModel: AnnouncementViewModel? = null,
    viewModelFactory: ViewModelFactory
) {
    val authState = authViewModel.authState.collectAsStateWithLifecycle()

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
            MainScreen(modifier, navController, authViewModel, getFoodViewModel, announcementViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(modifier, navController, authViewModel, profileViewModel)
        }
        composable(Screen.Tracking.route) {
            NutriTrackScreen(navController, authViewModel, getFoodViewModel)
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
                viewModel = getFoodViewModel,
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

            // Menggunakan Factory agar instance FoodViewModel mendapatkan Repository yang benar
            val foodViewModel: FoodViewModel = viewModel(factory = viewModelFactory)

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
            // Menggunakan Factory
            val foodViewModel: FoodViewModel = viewModel(factory = viewModelFactory)
            
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
            val viewModelGetFood: GetFoodViewModel = getFoodViewModel
            val foodItem by viewModelGetFood.food.collectAsStateWithLifecycle()
            
            // Menggunakan Factory
            val foodViewModel: FoodViewModel = viewModel(factory = viewModelFactory)

            EditFoodScreen(
                foodId = foodId,
                navController = navController,
                foodItem = foodItem,
                onUpdateFood = { id, imagePath, foodData ->
                    foodViewModel.updateFood(id, imagePath, foodData) 
                },
                onBack = { navController.popBackStack() },
                getFoodViewModel = viewModelGetFood,
                foodViewModel = foodViewModel 
            )
        }

        composable(Screen.AiChat.route) {
            AiChatScreen(
                aiChatViewModel = AiChatViewModel(),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("profile_onboarding?edit={edit}",
            arguments = listOf(navArgument("edit") {
                defaultValue = "false"
            })
        ) { backStackEntry ->
            val isEdit = backStackEntry.arguments?.getString("edit") == "true"

            ProfileOnboardingScreen(
                navController = navController,
                authViewModel = authViewModel,
                profileViewModel = profileViewModel,
                onboardingViewModel = onboardingViewModel,
                isEdit = isEdit
            )
        }
        composable(Screen.Recommendation.route) {
            val apiService = ApiConfig.getApiService()
            RecommendationScreen(
                viewModel = RecommendationViewModel(apiService),
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Help.route) {
            HelpScreen(navController)
        }
    }
}
