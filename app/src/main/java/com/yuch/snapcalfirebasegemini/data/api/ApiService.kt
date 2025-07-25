package com.yuch.snapcalfirebasegemini.data.api

import com.yuch.snapcalfirebasegemini.data.api.response.AiChatDelete
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatMessage
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatRequest
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatResponse
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeMyModelResponse
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.DailySummaryResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.FoodPage
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionEstimateRequest
import com.yuch.snapcalfirebasegemini.data.api.response.ProfileRequest
import com.yuch.snapcalfirebasegemini.data.api.response.RecommendationData
import com.yuch.snapcalfirebasegemini.data.api.response.UsageAiChat
import com.yuch.snapcalfirebasegemini.data.api.response.UserPreferences
import com.yuch.snapcalfirebasegemini.data.api.response.WeeklySummaryResponse
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
    @POST("food/analyze")
    suspend fun analyzeFood(
        @Part image: MultipartBody.Part,
        @Part("service") service: RequestBody
    ): Response<ApiResponse<AnalyzeResult>>

    // Analisis food pake model sendiri
    @Multipart
    @POST("food/analyze-by-mymodel")
    suspend fun analyzeFoodByMyModel(
        @Part file: MultipartBody.Part
    ): Response<AnalyzeMyModelResponse>

    // Estimate nutrition by food name after YOLO detection
    @POST("food/estimate-nutrition-by-name")
    suspend fun estimateNutritionByName(
        @Body request: NutritionEstimateRequest
    ): Response<ApiResponse<AnalyzeResult>>

    @Multipart
    @POST("food/upload")
    suspend fun uploadFood(
        @Part image: MultipartBody.Part?,
        @Part("foodName") foodName: RequestBody,
        @Part("mealType") mealType: RequestBody,
        @Part("weightInGrams") weightInGrams: RequestBody,
        @Part("nutritionData") nutritionData: RequestBody
    ): Response<ApiResponse<Food>>

    @GET("food/all")
    suspend fun getAllFood(@Query("page") page: Int = 1): Response<ApiResponse<FoodPage>>

    @GET("food")
    suspend fun getFoodEntries(@Query("date") date: String): Response<ApiResponse<List<FoodItem>>>

    @GET("food/{id}")
    suspend fun getFoodById(@Path("id") id: String): Response<ApiResponse<FoodItem>>

    @Multipart
    @PUT("food/{id}")
    suspend fun updateFood(
        @Path("id") id: String,
        @Part image: MultipartBody.Part?,
        @Part("foodName") foodName: RequestBody?,
        @Part("weightInGrams") weightInGrams: RequestBody?,
        @Part("nutritionData") nutritionData: RequestBody?,
        @Part("mealType") mealType: RequestBody?
    ): Response<ApiResponse<FoodItem>>

    @DELETE("food/{id}")
    suspend fun deleteFood(
        @Path("id") id: String
    ): Response<ApiResponse<FoodItem>>

    //Delete Food Image
    @DELETE("food/{id}/image")
    suspend fun deleteFoodImage(
        @Path("id") id: String
    ): Response<ApiResponse<FoodItem>>

    @POST("chat/start-ai")
    suspend fun aiMessage(
        @Body request: AiChatRequest
    ): Response<ApiResponse<AiChatResponse>>

    @GET("chat/history-ai")
    suspend fun getAiChatHistory(): Response<ApiResponse<List<AiChatMessage>>>

    @GET("chat/usage-ai")
    suspend fun getAiChatUsage(): Response<ApiResponse<UsageAiChat>>

    @DELETE("chat/history-ai")
    suspend fun deleteAiChatHistory(): Response<ApiResponse<AiChatDelete>>

    @GET("recommendations")
    suspend fun getRecommendation(
        @Query("mealType") mealType: String,
        @Query("refresh") refresh: Boolean = false
    ): Response<ApiResponse<RecommendationData>>

    @GET("food/summary")
    suspend fun getSummaryToday(): Response<ApiResponse<DailySummaryResponse>>

    @GET("food/summaryweekly")
    suspend fun getSummaryWeekly(): Response<ApiResponse<WeeklySummaryResponse>>

    @GET("profile")
    suspend fun getProfile(): Response<ApiResponse<UserPreferences>>

    @POST("profile")
    suspend fun postProfile(
        @Body request: ProfileRequest
    ): Response<ApiResponse<UserPreferences>>
//    @GET("api/chat/history")
//    suspend fun getChatHistory(): Response<List<ChatMessage>>
}