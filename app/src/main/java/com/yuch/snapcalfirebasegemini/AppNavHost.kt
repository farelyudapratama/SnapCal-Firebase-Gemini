package com.yuch.snapcalfirebasegemini

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yuch.snapcalfirebasegemini.view.LoginScreen
import com.yuch.snapcalfirebasegemini.view.MainScreen
import com.yuch.snapcalfirebasegemini.view.RegisterScreen
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(modifier, navController, authViewModel)
        }
        composable("register") {
            RegisterScreen(modifier, navController, authViewModel)
        }
        composable("home") {
            MainScreen(modifier, navController, authViewModel)
        }
    }
}