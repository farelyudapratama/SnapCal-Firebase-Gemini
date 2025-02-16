package com.yuch.snapcalfirebasegemini.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import com.yuch.snapcalfirebasegemini.data.api.response.FoodPage
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import com.yuch.snapcalfirebasegemini.utils.ImageUtils
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class ApiRepository(
    private val apiService: ApiService
) {
    suspend fun getAllFood(page: Int): ApiResponse<FoodPage> {
        return apiService.getAllFood(page).body()!!
    }

    suspend fun analyzeImage(imagePath: String, service: String): ApiResponse<AnalyzeResult> {
        val validationError = ImageUtils.validateImageFile(imagePath)
        if (validationError != null) throw Exception(validationError)

        val (imagePart, _) = ImageUtils.prepareImageForAnalyze(imagePath)
        val servicePart = service.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.analyzeFood(imagePart, servicePart).body() ?: throw Exception("Response kosong")
    }

    suspend fun uploadFood(imagePath: String?, foodData: EditableFoodData): ApiResponse<Food> {
        if (foodData.mealType == null) throw Exception("Please select a meal type")

        val imagePart = imagePath?.let {
            val validationError = ImageUtils.validateImageFile(it)
            if (validationError != null) throw Exception(validationError)
            ImageUtils.prepareImageForUpload(it).first
        }

        val foodNamePart = foodData.foodName.toRequestBody("text/plain".toMediaTypeOrNull())
        val mealTypePart = foodData.mealType!!.toRequestBody("text/plain".toMediaTypeOrNull())
        val nutritionJson = Gson().toJson(foodData)
        val nutritionPart = nutritionJson.toRequestBody("application/json".toMediaTypeOrNull())

        return apiService.uploadFood(imagePart, foodNamePart, mealTypePart, nutritionPart).body()
            ?: throw Exception("Response kosong")
    }
}

class FoodViewModelFactory(private val repository: ApiRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(
                FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
