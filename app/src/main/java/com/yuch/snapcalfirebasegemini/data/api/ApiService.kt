package com.yuch.snapcalfirebasegemini.data.api

import com.yuch.snapcalfirebasegemini.data.api.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @Multipart
    @POST("api/food/analyze/image")
    suspend fun analyzeFood(
        @Part image: MultipartBody.Part,
        @Part("service") service: RequestBody
    ): Response<ApiResponse<AnalyzeResult>>

    // Analisis food pake model sendiri
    @Multipart
    @POST("api/food/analyze/detect")
    suspend fun analyzeFoodByMyModel(
        @Part image: MultipartBody.Part
    ): Response<AnalyzeMyModelResponse>

    // Estimate nutrition by food name after YOLO detection
    @POST("api/food/analyze/text")
    suspend fun estimateNutritionByName(
        @Body request: NutritionEstimateRequest
    ): Response<ApiResponse<AnalyzeResult>>

    @Multipart
    @POST("api/food")
    suspend fun uploadFood(
        @Part image: MultipartBody.Part?,
        @Part("foodName") foodName: RequestBody,
        @Part("mealType") mealType: RequestBody,
        @Part("weightInGrams") weightInGrams: RequestBody,
        @Part("nutritionData") nutritionData: RequestBody
    ): Response<ApiResponse<Food>>

    @GET("api/food")
    suspend fun getAllFood(
        @Query("page") page: Int = 1,
        @Query("date") date: String? = null
    ): Response<FoodListResponse>

    @GET("api/food")
    suspend fun getFoodEntries(@Query("date") date: String): Response<FoodListResponse>

    @GET("api/food/{id}")
    suspend fun getFoodById(@Path("id") id: String): Response<ApiResponse<FoodItem>>

    @Multipart
    @PUT("api/food/{id}")
    suspend fun updateFood(
        @Path("id") id: String,
        @Part image: MultipartBody.Part?,
        @Part("foodName") foodName: RequestBody?,
        @Part("weightInGrams") weightInGrams: RequestBody?,
        @Part("nutritionData") nutritionData: RequestBody?,
        @Part("mealType") mealType: RequestBody?
    ): Response<ApiResponse<FoodItem>>

    @DELETE("api/food/{id}")
    suspend fun deleteFood(
        @Path("id") id: String
    ): Response<ApiResponse<FoodItem>>

    //Delete Food Image
    @DELETE("api/food/{id}/image")
    suspend fun deleteFoodImage(
        @Path("id") id: String
    ): Response<ApiResponse<FoodItem>>

    @POST("api/chat")
    suspend fun aiMessage(
        @Body request: AiChatRequest
    ): Response<ApiResponse<String>>

    @GET("api/chat")
    suspend fun getAiChatHistory(): Response<ApiResponse<List<AiChatMessage>>>

    @GET("api/usage")
    suspend fun getUsage(): Response<ApiResponse<UsageResponse>>

    @DELETE("api/chat")
    suspend fun deleteAiChatHistory(): Response<ApiResponse<AiChatDelete>>

    @GET("api/recommendations")
    suspend fun getRecommendation(
        @Query("mealType") mealType: String,
        @Query("refresh") refresh: Boolean = false
    ): Response<ApiResponse<RecommendationData>>

    @GET("api/food/summary/daily")
    suspend fun getSummaryToday(@Query("date") date: String? = null): Response<ApiResponse<DailySummaryResponse>>

    @GET("api/food/summary/weekly")
    suspend fun getSummaryWeekly(): Response<ApiResponse<WeeklySummaryResponse>>

    @GET("api/profile")
    suspend fun getProfile(): Response<ApiResponse<UserPreferences>>

    @POST("api/profile")
    suspend fun postProfile(
        @Body request: ProfileRequest
    ): Response<ApiResponse<UserPreferences>>

    @DELETE("api/profile")
    suspend fun deleteProfile(): Response<ApiResponse<Any>>

    @GET("api/announcements/active")
    suspend fun getActiveAnnouncements(): Response<ApiResponse<List<Announcement>>>
}
