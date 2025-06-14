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
import com.yuch.snapcalfirebasegemini.data.di.AiChatViewModelFactory
import com.yuch.snapcalfirebasegemini.data.di.AuthViewModelFactory
import com.yuch.snapcalfirebasegemini.data.di.FoodViewModelFactory
import com.yuch.snapcalfirebasegemini.data.di.OnboardingViewModelFactory
import com.yuch.snapcalfirebasegemini.data.di.ProfileViewModelFactory
import com.yuch.snapcalfirebasegemini.data.local.AppDatabase
import com.yuch.snapcalfirebasegemini.viewmodel.AiChatViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.OnboardingViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {

    // Authentication ViewModel
    private val authViewModel: AuthViewModel by viewModels { 
        AuthViewModelFactory() 
    }
    
    // Camera ViewModel
    private val cameraViewModel: CameraViewModel by viewModels()
    
    // ViewModel dengan database lokal
    private val appDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "snapcal_database"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .fallbackToDestructiveMigration() // Gunakan ini hanya jika data lokal dapat dihapus saat migrasi gagal
        .build()
    }
    
    // API service
    private val apiService by lazy {
        ApiConfig.getApiService()
    }
    
    // Food ViewModel (Combined version that replaces GetFoodViewModel)
    private val foodViewModel: FoodViewModel by viewModels {
        FoodViewModelFactory.getInstance(
            apiService = apiService,
            foodDao = appDatabase.foodDao()
        )
    }
    
    // Profile ViewModel
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory.getInstance(apiService)
    }
    
    // Onboarding ViewModel (now connected to ProfileRepository)
    private val onboardingViewModel: OnboardingViewModel by viewModels {
        OnboardingViewModelFactory.getInstance(apiService)
    }
    
    // AI Chat ViewModel
    private val aiChatViewModel: AiChatViewModel by viewModels {
        AiChatViewModelFactory.getInstance(apiService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MaterialTheme {
                SnapCalApp(
                    authViewModel = authViewModel,
                    cameraViewModel = cameraViewModel,
                    foodViewModel = foodViewModel,
                    profileViewModel = profileViewModel,
                    onboardingViewModel = onboardingViewModel,
                    aiChatViewModel = aiChatViewModel
                )
            }
        }
    }
}
