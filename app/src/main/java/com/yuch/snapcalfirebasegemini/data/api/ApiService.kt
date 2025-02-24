package com.yuch.snapcalfirebasegemini.data.api

import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.ChatRequest
import com.yuch.snapcalfirebasegemini.data.api.response.ChatResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.FoodPage
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
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

    @Multipart
    @POST("food/upload")
    suspend fun uploadFood(
        @Part image: MultipartBody.Part?,
        @Part("foodName") foodName: RequestBody,
        @Part("mealType") mealType: RequestBody,
        @Part("nutritionData") nutritionData: RequestBody
    ): Response<ApiResponse<Food>>

    @GET("food/all")
    suspend fun getAllFood(@Query("page") page: Int = 1): Response<ApiResponse<FoodPage>>

    @GET("food/{id}")
    suspend fun getFoodById(@Path("id") id: String): Response<ApiResponse<FoodItem>>

    @Multipart
    @PUT("food/{id}")
    suspend fun updateFood(
        @Path("id") id: String,
        @Part image: MultipartBody.Part?,
        @Part("foodName") foodName: RequestBody?,
        @Part("nutritionData") nutritionData: RequestBody?,
        @Part("mealType") mealType: RequestBody?
    ): Response<ApiResponse<FoodItem>>

    @POST("api/chat/start")
    suspend fun sendMessage(
        @Body message: ChatRequest
    ): Response<ChatResponse>

//    @GET("api/chat/history")
//    suspend fun getChatHistory(): Response<List<ChatMessage>>
}