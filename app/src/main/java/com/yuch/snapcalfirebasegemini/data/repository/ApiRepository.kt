package com.yuch.snapcalfirebasegemini.data.repository

import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.ChatRequest
import okhttp3.MultipartBody

class ApiRepository private constructor(
    private val apiService: ApiService
) {
    suspend fun analyzeFood(image: MultipartBody.Part, service: String) = apiService.analyzeFood(image, service)
    suspend fun sendMessage(message: ChatRequest) = apiService.sendMessage(message)



    companion object {
        @Volatile
        private var instance: ApiRepository? = null
        fun getInstance(
            apiService: ApiService,
        ): ApiRepository =
            instance ?: synchronized(this) {
                instance ?: ApiRepository(apiService)
            }
    }
}