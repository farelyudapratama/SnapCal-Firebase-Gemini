package com.yuch.snapcalfirebasegemini.di

import android.content.Context
import androidx.room.Room
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.local.AppDatabase
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.data.repository.ProfileRepository
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

    // 2. ApiService Dibuat HANYA SEKALI di sini
    val apiService by lazy {
        ApiConfig.getApiService()
    }

    // 3. Repository (Keduanya berbagi instance apiService yang sama)
    val apiRepository by lazy {
        ApiRepository(
            apiService = apiService,
            foodDao = database.foodDao()
        )
    }

    val profileRepository by lazy {
        // Jika kamu menghapus getInstance() di ProfileRepository, ubah jadi ProfileRepository(apiService)
        ProfileRepository.getInstance(apiService)
    }

    // 4. ViewModel Factory Tunggal
    val viewModelFactory by lazy {
        ViewModelFactory(apiRepository, profileRepository)
    }
}