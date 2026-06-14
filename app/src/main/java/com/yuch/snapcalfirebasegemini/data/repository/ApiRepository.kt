package com.yuch.snapcalfirebasegemini.data.repository

import com.google.gson.Gson
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatDelete
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatMessage
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatRequest
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeMyModelResponse
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.Announcement
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.DailySummaryResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.FoodListResponse
import com.yuch.snapcalfirebasegemini.data.api.response.FoodPage
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionEstimateRequest
import com.yuch.snapcalfirebasegemini.data.api.response.RecommendationData
import com.yuch.snapcalfirebasegemini.data.api.response.UsageResponse
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
        val result = safeFoodListCall { apiService.getAllFood(page) }
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
        return when (val result = safeFoodListCall { apiService.getFoodEntries(date) }) {
            is AppResult.Success -> AppResult.Success(result.data.items, result.message)
            is AppResult.Error -> result
        }
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
                    errorCode = body?.code ?: body?.error?.code?.toString()
                )
            }
        } catch (e: Exception) {
            AppResult.Error(
                message = e.message ?: "Request failed",
                cause = e
            )
        }
    }

    private suspend fun safeFoodListCall(call: suspend () -> Response<FoodListResponse>): AppResult<FoodPage> {
        return try {
            val response = call()
            val body = response.body()

            if (response.isSuccessful && body?.status == "success") {
                val pagination = body.pagination
                AppResult.Success(
                    FoodPage(
                        page = pagination?.page ?: 1,
                        totalPages = pagination?.totalPages ?: 1,
                        totalItems = pagination?.total ?: body.data.size,
                        items = body.data
                    ),
                    body.message
                )
            } else {
                AppResult.Error(
                    message = body?.message ?: parseErrorMessage(response),
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
            ?: emptyList()
    }

    suspend fun getSummaryToday(date: String? = null): AppResult<DailySummaryResponse> {
        return safeApiResponseCall { apiService.getSummaryToday(date) }
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

    suspend fun sendAiMessage(request: AiChatRequest): AppResult<String> {
        return safeApiResponseCall { apiService.aiMessage(request) }
    }

    suspend fun getUsage(): AppResult<UsageResponse> {
        return when (val result = safeUsageCall { apiService.getUsage() }) {
            is AppResult.Success -> result
            is AppResult.Error -> result
        }
    }

    private suspend fun safeUsageCall(call: suspend () -> Response<ApiResponse<UsageResponse>>): AppResult<UsageResponse> {
        return try {
            val response = call()
            val body = response.body()

            if (response.isSuccessful && body?.status == "success" && body.data != null) {
                AppResult.Success(body.data.copy(byokActive = body.byokActive), body.message)
            } else {
                AppResult.Error(
                    message = body?.message ?: parseErrorMessage(response),
                    code = response.code(),
                    errorCode = body?.code ?: body?.error?.code?.toString()
                )
            }
        } catch (e: Exception) {
            AppResult.Error(
                message = e.message ?: "Request failed",
                cause = e
            )
        }
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

    suspend fun deleteProfile(): AppResult<Unit> {
        return when (val result = safeApiResponseCall { apiService.deleteProfile() }) {
            is AppResult.Success -> AppResult.Success(Unit, result.message)
            is AppResult.Error -> result
        }
    }

}
