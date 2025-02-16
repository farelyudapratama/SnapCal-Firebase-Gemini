package com.yuch.snapcalfirebasegemini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.room.Room
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.local.AppDatabase
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.data.repository.FoodViewModelFactory
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val cameraViewModel: CameraViewModel by viewModels()
    private val foodViewModel: FoodViewModel by viewModels {
        FoodViewModelFactory(
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
        setContent {
            MaterialTheme {
                SnapCalApp(authViewModel = authViewModel, cameraViewModel = cameraViewModel, foodViewModel = foodViewModel)
            }
        }
    }
}
