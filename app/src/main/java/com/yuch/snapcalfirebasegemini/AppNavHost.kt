package com.yuch.snapcalfirebasegemini

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.view.AnalyzeScreen
import com.yuch.snapcalfirebasegemini.view.LoginScreen
import com.yuch.snapcalfirebasegemini.view.MainScreen
import com.yuch.snapcalfirebasegemini.view.ManualEntryScreen
import com.yuch.snapcalfirebasegemini.view.ProfileScreen
import com.yuch.snapcalfirebasegemini.view.RegisterScreen
import com.yuch.snapcalfirebasegemini.view.camera.ScanScreen
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel,

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
        composable(Screen.Main.route) {
            MainScreen(modifier, navController, authViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(modifier, navController, authViewModel)
        }
        composable(Screen.Scan.route) {
            ScanScreen(
                modifier, navController, authViewModel, viewModel = CameraViewModel(),
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
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ManualEntry.route) {
            ManualEntryScreen(
                modifier,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
