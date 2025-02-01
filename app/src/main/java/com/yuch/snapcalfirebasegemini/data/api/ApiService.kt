package com.yuch.snapcalfirebasegemini.data.api

import com.yuch.snapcalfirebasegemini.data.api.response.ChatRequest
import com.yuch.snapcalfirebasegemini.data.api.response.ChatResponse
import com.yuch.snapcalfirebasegemini.data.api.response.FoodAnalysisResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("api/food/analyze")
    suspend fun analyzeFood(
        @Part image: MultipartBody.Part,
        @Part("service") service: String
    ): Response<FoodAnalysisResponse>

    @POST("api/chat/start")
    suspend fun sendMessage(
        @Body message: ChatRequest
    ): Response<ChatResponse>

//    @GET("api/chat/history")
//    suspend fun getChatHistory(): Response<List<ChatMessage>>
}