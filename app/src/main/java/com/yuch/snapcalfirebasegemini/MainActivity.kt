package com.yuch.snapcalfirebasegemini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.local.AppDatabase
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.data.repository.ProfileRepository
import com.yuch.snapcalfirebasegemini.data.repository.ProfileViewModelFactory
import com.yuch.snapcalfirebasegemini.data.repository.ViewModelFactory
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.OnboardingViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel

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
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ApiConfig.getApiService())
    }
    private val onboardingViewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Setup callback untuk menghapus semua data saat logout
        setupClearDataCallback()

        setContent {
            MaterialTheme {
                SnapCalApp(authViewModel = authViewModel, cameraViewModel = cameraViewModel, getFoodViewModel = getFoodViewModel,
                    profileViewModel = profileViewModel, onboardingViewModel = onboardingViewModel)
            }
        }
    }

    private fun setupClearDataCallback() {
        authViewModel.setClearDataCallback {
            // Hapus data dari semua ViewModels
            cameraViewModel.clearData()
            getFoodViewModel.clearData()
            profileViewModel.clearData()
            onboardingViewModel.clearData()
        }
    }
}
