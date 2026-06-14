package com.yuch.snapcalfirebasegemini.di

import android.content.Context
import androidx.room.Room
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.local.AppDatabase
import com.yuch.snapcalfirebasegemini.data.local.FoodDao
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.data.repository.ProfileRepository
import com.yuch.snapcalfirebasegemini.domain.auth.TokenManager
import com.yuch.snapcalfirebasegemini.viewmodel.ViewModelFactory

class AppContainer(private val context: Context) {

    // 1. Database
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "snapcal_database"
        ).build()
    }

    val foodDao: FoodDao by lazy {
        database.foodDao()
    }

    // 2. ApiService Dibuat HANYA SEKALI di sini
    val apiService: ApiService by lazy {
        ApiConfig.getApiService()
    }

    // 3. Repository (Keduanya berbagi instance apiService yang sama)
    val apiRepository by lazy {
        ApiRepository(
            apiService = apiService,
            foodDao = foodDao
        )
    }

    val profileRepository by lazy {
        ProfileRepository(apiService)
    }

    val tokenManager: TokenManager by lazy {
        object : TokenManager {
            override fun clearToken() = ApiConfig.clearTokenCache()
        }
    }

    // 4. ViewModel Factory Tunggal
    val viewModelFactory by lazy {
        ViewModelFactory(apiRepository, profileRepository, tokenManager)
    }
}
