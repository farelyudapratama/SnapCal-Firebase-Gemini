package com.yuch.snapcalfirebasegemini.data.repository

import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatDelete
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatMessage
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatRequest
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatResponse
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeMyModelResponse
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.Announcement
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.DailySummaryResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.FoodPage
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionEstimateRequest
import com.yuch.snapcalfirebasegemini.data.api.response.RecommendationData
import com.yuch.snapcalfirebasegemini.data.api.response.UsageAiChat
import com.yuch.snapcalfirebasegemini.data.api.response.UserPreferences
import com.yuch.snapcalfirebasegemini.data.api.response.WeeklySummaryResponse
import com.yuch.snapcalfirebasegemini.data.local.FoodDao
import com.yuch.snapcalfirebasegemini.data.local.FoodEntity
import com.yuch.snapcalfirebasegemini.data.mapper.toEntity
import com.yuch.snapcalfirebasegemini.data.mapper.toFoodItem
import com.yuch.snapcalfirebasegemini.domain.result.AppResult
import com.yuch.snapcalfirebasegemini.utils.parseCreatedAt
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class ApiRepository(
    private val apiService: ApiService,
    private val foodDao: FoodDao?
) {

    suspend fun analyzeFood(image: MultipartBody.Part, service: RequestBody): Response<ApiResponse<AnalyzeResult>> {
        return apiService.analyzeFood(image, service)
    }

    suspend fun analyzeFoodByMyModel(file: MultipartBody.Part): Response<AnalyzeMyModelResponse> {
        return apiService.analyzeFoodByMyModel(file)
    }

    suspend fun estimateNutritionByName(request: NutritionEstimateRequest): Response<ApiResponse<AnalyzeResult>> {
        return apiService.estimateNutritionByName(request)
    }

    suspend fun uploadFood(
        image: MultipartBody.Part?,
        foodName: RequestBody,
        mealType: RequestBody,
        weightInGrams: RequestBody,
        nutritionData: RequestBody
    ): Response<ApiResponse<Food>> {
        return apiService.uploadFood(image, foodName, mealType, weightInGrams, nutritionData)
    }

    suspend fun updateFood(
        id: String,
        image: MultipartBody.Part?,
        foodName: RequestBody?,
        weightInGrams: RequestBody?,
        nutritionData: RequestBody?,
        mealType: RequestBody?
    ): Response<ApiResponse<FoodItem>> {
        return apiService.updateFood(id, image, foodName, weightInGrams, nutritionData, mealType)
    }

    suspend fun getAllFood(page: Int): ApiResponse<FoodPage>? {
        val response = apiService.getAllFood(page)
        val body =
            response.body()
                ?: return null

        val foodEntities = body.data?.items?.map { food -> food.toEntity() }

        // Simpan data ke Room jika tidak null
        foodEntities?.let { foodDao?.insertFoods(it) }

        // Hapus data yang lebih lama dari 7 hari
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        foodDao?.deleteOldFoods(sevenDaysAgo)

        return body
    }

    suspend fun getFoodDate(date: String): ApiResponse<List<FoodItem>>? {
        return when (val result = safeApiCall { apiService.getFoodEntries(date) }) {
            is AppResult.Success -> result.data
            is AppResult.Error -> null
        }
    }

    suspend fun getFoodById(id: String, forceRefresh: Boolean = false): FoodItem? {
        if (!forceRefresh) {
            foodDao?.getFoodById(id)?.let { cachedFood ->
                return cachedFood.toFoodItem()
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
                                newData.toEntity()
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

    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): AppResult<T> {
        return try {
            val response = call()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                AppResult.Success(body)
            } else {
                AppResult.Error(
                    message = response.message().ifBlank { "Request failed" },
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            AppResult.Error(
                message = e.message ?: "Request failed",
                cause = e
            )
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

    suspend fun getSummaryToday(): ApiResponse<DailySummaryResponse>? {
        return try {
            val response = apiService.getSummaryToday()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSummaryWeek(): ApiResponse<WeeklySummaryResponse>? {
        return try {
            val response = apiService.getSummaryWeekly()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getProfile(): ApiResponse<UserPreferences>? {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getActiveAnnouncements(): ApiResponse<List<Announcement>>? {
        return try {
            val response = apiService.getActiveAnnouncements()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // --- AI Chat ---

    suspend fun getAiChatHistory(): Response<ApiResponse<List<AiChatMessage>>> {
        return apiService.getAiChatHistory()
    }

    suspend fun sendAiMessage(request: AiChatRequest): Response<ApiResponse<AiChatResponse>> {
        return apiService.aiMessage(request)
    }

    suspend fun getAiChatUsage(): Response<ApiResponse<UsageAiChat>> {
        return apiService.getAiChatUsage()
    }

    suspend fun deleteAiChatHistory(): Response<ApiResponse<AiChatDelete>> {
        return apiService.deleteAiChatHistory()
    }

    // --- Recommendations ---

    suspend fun getRecommendation(
        mealType: String,
        refresh: Boolean = false
    ): Response<ApiResponse<RecommendationData>> {
        return apiService.getRecommendation(mealType, refresh)
    }
}
