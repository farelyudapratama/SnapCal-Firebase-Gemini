package com.yuch.snapcalfirebasegemini.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import com.yuch.snapcalfirebasegemini.data.api.response.FoodPage
import com.yuch.snapcalfirebasegemini.data.local.FoodDao
import com.yuch.snapcalfirebasegemini.data.local.FoodEntity
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import com.yuch.snapcalfirebasegemini.utils.ImageUtils
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class ApiRepository(
    private val apiService: ApiService,
    private val foodDao: FoodDao?
) {
    suspend fun getAllFood(page: Int): ApiResponse<FoodPage> {
        val response = apiService.getAllFood(page).body()!!
        val foodEntities =
            response.data?.items?.map { food ->
                FoodEntity(
                    id = food.id,
                    userId = food.userId,
                    foodName = food.foodName,
                    imageUrl = food.imageUrl,
                    mealType = food.mealType,
                    calories = food.nutritionData.calories,
                    carbs = food.nutritionData.carbs,
                    protein = food.nutritionData.protein,
                    totalFat = food.nutritionData.totalFat,
                    saturatedFat = food.nutritionData.saturatedFat,
                    fiber = food.nutritionData.fiber,
                    sugar = food.nutritionData.sugar,
                    // Asumsikan createdAt dari API dalam format ISO, konversi ke milidetik
                    createdAt = parseCreatedAt(food.createdAt)
                )
            }
        // Simpan data ke Room
        if (foodEntities != null) {
            foodDao?.insertFoods(foodEntities)
        }
        // Hapus data yang lebih lama dari 7 hari
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        foodDao?.deleteOldFoods(sevenDaysAgo)
        return response
    }

    suspend fun getCachedFoods(): List<FoodEntity> {
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        return foodDao?.getRecentFoods(sevenDaysAgo)
            ?: throw Exception("Database tidak tersedia")
    }

    private fun parseCreatedAt(createdAt: String): Long {
        // Implementasikan parsing sesuai format tanggal API Anda
        // Contoh sederhana: gunakan Instant.parse dan toEpochMilli jika format ISO-8601
        return try {
            java.time.Instant.parse(createdAt).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}

class ViewModelFactory(private val repository: ApiRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
//            modelClass.isAssignableFrom(FoodViewModel::class.java) -> FoodViewModel(repository) as T
            modelClass.isAssignableFrom(GetFoodViewModel::class.java) -> GetFoodViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
