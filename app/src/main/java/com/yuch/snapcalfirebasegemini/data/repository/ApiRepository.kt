package com.yuch.snapcalfirebasegemini.data.repository

import android.net.Uri
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.ChatRequest
import com.yuch.snapcalfirebasegemini.data.api.response.FoodAnalysisResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ApiRepository private constructor(
    private val apiService: ApiService
) {
//    suspend fun analyzeFood(image: MultipartBody.Part, service: String) = apiService.analyzeFood(image, service)
//    suspend fun sendMessage(message: ChatRequest) = apiService.sendMessage(message)

    suspend fun analyzeFood(imageUri: Uri, service: String): Result<ApiResponse<AnalyzeResult>> {
        return try {
            val file = File(imageUri.path!!)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())

            // Create multipart parts
            val imagePart = MultipartBody.Part.createFormData(
                "image",
                file.name,
                requestFile
            )

            val servicePart = service.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = ApiConfig.getApiService().analyzeFood(imagePart, servicePart)

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

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