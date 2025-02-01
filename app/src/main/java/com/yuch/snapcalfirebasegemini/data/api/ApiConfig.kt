package com.yuch.snapcalfirebasegemini.data.api

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiConfig {
    companion object {
        const val BASE_URL =
            ""

        private fun getFirebaseToken(): String {
            return Firebase.auth.currentUser?.uid ?: throw Exception("User not authenticated")
        }

        fun getApiService(): ApiService {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer ${getFirebaseToken()}")
                        .build()
                    chain.proceed(request)
                }
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(
                    GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}