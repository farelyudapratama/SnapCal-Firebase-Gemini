package com.yuch.snapcalfirebasegemini.di

import android.content.Context
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository

object Injection {
    fun provideRepository(context: Context): ApiRepository {
        val apiService = ApiConfig.getApiService()
        return ApiRepository.getInstance(apiService)
    }
}