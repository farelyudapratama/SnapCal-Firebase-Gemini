package com.yuch.snapcalfirebasegemini.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.FoodPage
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionData
import com.yuch.snapcalfirebasegemini.data.local.FoodDao
import com.yuch.snapcalfirebasegemini.data.local.FoodEntity
import com.yuch.snapcalfirebasegemini.utils.parseCreatedAt
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel

class ApiRepository(
    private val apiService: ApiService,
    private val foodDao: FoodDao?
) {

    // TODO belom sempurna karena masih perlu refresh
    suspend fun getAllFood(page: Int): ApiResponse<FoodPage>? {
        val response = apiService.getAllFood(page)
        val body =
            response.body()
                ?: return null

        val foodEntities = body.data?.items?.map { food ->
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
                createdAt = parseCreatedAt(food.createdAt)
            )
        }

        // Simpan data ke Room jika tidak null
        foodEntities?.let { foodDao?.insertFoods(it) }

        // Hapus data yang lebih lama dari 7 hari
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        foodDao?.deleteOldFoods(sevenDaysAgo)

        return body
    }

    suspend fun getFoodById(id: String, forceRefresh: Boolean = false): FoodItem? {
        if (!forceRefresh) {
            foodDao?.getFoodById(id)?.let { cachedFood ->
                return FoodItem(
                    id = cachedFood.id,
                    userId = cachedFood.userId,
                    foodName = cachedFood.foodName,
                    mealType = cachedFood.mealType,
                    nutritionData = NutritionData(
                        calories = cachedFood.calories,
                        carbs = cachedFood.carbs,
                        protein = cachedFood.protein,
                        totalFat = cachedFood.totalFat,
                        saturatedFat = cachedFood.saturatedFat,
                        fiber = cachedFood.fiber,
                        sugar = cachedFood.sugar
                    ),
                    imageUrl = cachedFood.imageUrl,
                    createdAt = parseCreatedAt(cachedFood.createdAt.toString()).toString()
                )
            }
        }

        // Ambil data terbaru dari API
        return try {
            val response = apiService.getFoodById(id)
            if (response.isSuccessful && response.body()?.data != null) {
                response.body()?.data?.also { newData ->
                    val createdAtMillis = parseCreatedAt(newData.createdAt)
                    val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
                    if (createdAtMillis >= sevenDaysAgo) {
                        // Simpan data terbaru ke Room agar cache diperbarui
                        foodDao?.insertFoods(
                            listOf(
                                FoodEntity(
                                    id = newData.id,
                                    userId = newData.userId,
                                    foodName = newData.foodName,
                                    imageUrl = newData.imageUrl,
                                    mealType = newData.mealType,
                                    calories = newData.nutritionData.calories,
                                    carbs = newData.nutritionData.carbs,
                                    protein = newData.nutritionData.protein,
                                    totalFat = newData.nutritionData.totalFat,
                                    saturatedFat = newData.nutritionData.saturatedFat,
                                    fiber = newData.nutritionData.fiber,
                                    sugar = newData.nutritionData.sugar,
                                    createdAt = parseCreatedAt(
                                        newData.createdAt
                                    )
                                )
                            )
                        )
                    }
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteFood(id: String): ApiResponse<FoodItem>? {
        return try {
            val response = apiService.deleteFood(id)
            if (response.isSuccessful) {
                foodDao?.deleteFoodById(id)
            }
            response.body()
        } catch (e: Exception) {
            null
        }
    }

    // Delete Image Food
    suspend fun deleteFoodImage(id: String): ApiResponse<FoodItem>? {
        return try {
            val response = apiService.deleteFoodImage(id)
            if (response.isSuccessful) {
                foodDao?.deleteFoodImageById(id)
            }
            response.body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCachedFoods(): List<FoodEntity> {
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        return foodDao?.getRecentFoods(sevenDaysAgo)
            ?: throw Exception("Database tidak tersedia")
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
