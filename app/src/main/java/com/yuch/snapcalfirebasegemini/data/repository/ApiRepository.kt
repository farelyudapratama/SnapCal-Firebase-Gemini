package com.yuch.snapcalfirebasegemini.data.repository

import com.google.gson.Gson
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

    suspend fun analyzeFood(image: MultipartBody.Part, service: RequestBody): AppResult<AnalyzeResult> {
        return safeApiResponseCall { apiService.analyzeFood(image, service) }
    }

    suspend fun analyzeFoodByMyModel(file: MultipartBody.Part): AppResult<AnalyzeMyModelResponse> {
        return safeApiCall { apiService.analyzeFoodByMyModel(file) }
    }

    suspend fun estimateNutritionByName(request: NutritionEstimateRequest): AppResult<AnalyzeResult> {
        return safeApiResponseCall { apiService.estimateNutritionByName(request) }
    }

    suspend fun uploadFood(
        image: MultipartBody.Part?,
        foodName: RequestBody,
        mealType: RequestBody,
        weightInGrams: RequestBody,
        nutritionData: RequestBody
    ): AppResult<Food> {
        return safeApiResponseCall { apiService.uploadFood(image, foodName, mealType, weightInGrams, nutritionData) }
    }

    suspend fun updateFood(
        id: String,
        image: MultipartBody.Part?,
        foodName: RequestBody?,
        weightInGrams: RequestBody?,
        nutritionData: RequestBody?,
        mealType: RequestBody?
    ): AppResult<FoodItem> {
        return safeApiResponseCall { apiService.updateFood(id, image, foodName, weightInGrams, nutritionData, mealType) }
    }

    suspend fun getAllFood(page: Int): AppResult<FoodPage> {
        val result = safeApiResponseCall { apiService.getAllFood(page) }
        val foodPage = (result as? AppResult.Success)?.data
        val foodEntities = foodPage?.items?.map { food -> food.toEntity() }

        // Simpan data ke Room jika tidak null
        foodEntities?.let { foodDao?.insertFoods(it) }

        // Hapus data yang lebih lama dari 7 hari
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        foodDao?.deleteOldFoods(sevenDaysAgo)

        return result
    }

    suspend fun getFoodDate(date: String): AppResult<List<FoodItem>> {
        return safeApiResponseCall { apiService.getFoodEntries(date) }
    }

    suspend fun getFoodById(id: String, forceRefresh: Boolean = false): AppResult<FoodItem> {
        if (!forceRefresh) {
            foodDao?.getFoodById(id)?.let { cachedFood ->
                return AppResult.Success(cachedFood.toFoodItem())
            }
        }

        // Ambil data terbaru dari API
        val result = safeApiResponseCall { apiService.getFoodById(id) }
        (result as? AppResult.Success)?.data?.let { newData ->
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
        return result
    }

    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): AppResult<T> {
        return try {
            val response = call()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                AppResult.Success(body)
            } else {
                AppResult.Error(
                    message = parseErrorMessage(response),
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

    private suspend fun <T> safeApiResponseCall(call: suspend () -> Response<ApiResponse<T>>): AppResult<T> {
        return try {
            val response = call()
            val body = response.body()

            if (response.isSuccessful && body?.status == "success" && body.data != null) {
                AppResult.Success(body.data, body.message)
            } else {
                AppResult.Error(
                    message = body?.message ?: parseErrorMessage(response),
                    code = response.code(),
                    errorCode = body?.error?.code?.toString()
                )
            }
        } catch (e: Exception) {
            AppResult.Error(
                message = e.message ?: "Request failed",
                cause = e
            )
        }
    }

    private fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ApiResponse::class.java)
            errorResponse?.message ?: response.message().ifBlank { "Request failed" }
        } catch (e: Exception) {
            response.message().ifBlank { "Request failed" }
        }
    }

    suspend fun deleteFood(id: String): AppResult<FoodItem> {
        val result = safeApiResponseCall { apiService.deleteFood(id) }
        if (result is AppResult.Success) {
            foodDao?.deleteFoodById(id)
        }
        return result
    }

    // Delete Image Food
    suspend fun deleteFoodImage(id: String): AppResult<FoodItem> {
        val result = safeApiResponseCall { apiService.deleteFoodImage(id) }
        if (result is AppResult.Success) {
            foodDao?.deleteFoodImageById(id)
        }
        return result
    }

    suspend fun getCachedFoods(): List<FoodEntity> {
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        return foodDao?.getRecentFoods(sevenDaysAgo)
            ?: throw Exception("Database tidak tersedia")
    }

    suspend fun getSummaryToday(): AppResult<DailySummaryResponse> {
        return safeApiResponseCall { apiService.getSummaryToday() }
    }

    suspend fun getSummaryWeek(): AppResult<WeeklySummaryResponse> {
        return safeApiResponseCall { apiService.getSummaryWeekly() }
    }

    suspend fun getProfile(): AppResult<UserPreferences> {
        return safeApiResponseCall { apiService.getProfile() }
    }

    suspend fun getActiveAnnouncements(): AppResult<List<Announcement>> {
        return safeApiResponseCall { apiService.getActiveAnnouncements() }
    }

    // --- AI Chat ---

    suspend fun getAiChatHistory(): AppResult<List<AiChatMessage>> {
        return safeApiResponseCall { apiService.getAiChatHistory() }
    }

    suspend fun sendAiMessage(request: AiChatRequest): AppResult<AiChatResponse> {
        return safeApiResponseCall { apiService.aiMessage(request) }
    }

    suspend fun getAiChatUsage(): AppResult<UsageAiChat> {
        return safeApiResponseCall { apiService.getAiChatUsage() }
    }

    suspend fun deleteAiChatHistory(): AppResult<AiChatDelete> {
        return safeApiResponseCall { apiService.deleteAiChatHistory() }
    }

    // --- Recommendations ---

    suspend fun getRecommendation(
        mealType: String,
        refresh: Boolean = false
    ): AppResult<RecommendationData> {
        return safeApiResponseCall { apiService.getRecommendation(mealType, refresh) }
    }
}
