package com.yuch.snapcalfirebasegemini.data.api

import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.ChatRequest
import com.yuch.snapcalfirebasegemini.data.api.response.ChatResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("food/analyze")
    suspend fun analyzeFood(
        @Part image: MultipartBody.Part,
        @Part("service") service: RequestBody
    ): Response<ApiResponse<AnalyzeResult>>

    @Multipart
    @POST("food/upload")
    suspend fun uploadFood(
        @Part image: MultipartBody.Part?,
        @Part("foodName") foodName: RequestBody,
        @Part("mealType") mealType: RequestBody,
        @Part("nutritionData") nutritionData: RequestBody
    ): Response<ApiResponse<Food>>

    @POST("api/chat/start")
    suspend fun sendMessage(
        @Body message: ChatRequest
    ): Response<ChatResponse>

//    @GET("api/chat/history")
//    suspend fun getChatHistory(): Response<List<ChatMessage>>
}