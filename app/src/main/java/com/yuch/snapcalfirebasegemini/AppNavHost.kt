package com.yuch.snapcalfirebasegemini

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.view.LoginScreen
import com.yuch.snapcalfirebasegemini.view.MainScreen
import com.yuch.snapcalfirebasegemini.view.ProfileScreen
import com.yuch.snapcalfirebasegemini.view.RegisterScreen
import com.yuch.snapcalfirebasegemini.view.camera.ScanScreen
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
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
    }
}
