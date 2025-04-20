package com.yuch.snapcalfirebasegemini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.view.WindowCompat
import androidx.room.Room
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.local.AppDatabase
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.data.repository.ViewModelFactory
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val cameraViewModel: CameraViewModel by viewModels()
    private val getFoodViewModel: GetFoodViewModel by viewModels {
        ViewModelFactory(
            ApiRepository(
                apiService = ApiConfig.getApiService(),
                foodDao = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "snapcal_database"
                ).build().foodDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MaterialTheme {
                SnapCalApp(authViewModel = authViewModel, cameraViewModel = cameraViewModel, getFoodViewModel = getFoodViewModel)
            }
        }
    }
}
