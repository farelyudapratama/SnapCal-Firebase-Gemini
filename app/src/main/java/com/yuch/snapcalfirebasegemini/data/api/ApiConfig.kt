package com.yuch.snapcalfirebasegemini.data.api

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Headers
import retrofit2.http.GET
import kotlinx.coroutines.tasks.await
import okhttp3.ResponseBody.Companion.toResponseBody

class ApiConfig {

    companion object {

        private const val BASE_URL = ""

        // Gunakan suspend function untuk asynchronous token retrieval
        private suspend fun getFirebaseToken(): String {
            val user = FirebaseAuth.getInstance().currentUser
                ?: throw Exception("User not authenticated")

            // Menggunakan await() untuk menunggu id token dengan Kotlin coroutines
            return try {
                // Add timeout for token retrieval
                withContext(Dispatchers.IO) {
                    kotlinx.coroutines.withTimeout(10000) { // 10 second timeout
                        user.getIdToken(true).await()?.token
                            ?: throw Exception("Token is null")
                    }
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                throw Exception("Token retrieval timeout after 10 seconds")
            } catch (e: Exception) {
                throw Exception("Token retrieval failed: ${e.message}")
            }
        }

        fun getApiService(): ApiService {
            val okHttpClient = OkHttpClient.Builder()
                // Add timeout configurations
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .callTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val request = chain.request()

                    // Menjalankan fungsi async di dalam thread background
                    val token = try {
                        // Gunakan CoroutineScope.launch untuk menjalankan async token retrieval
                        runBlocking(Dispatchers.IO) {
                            getFirebaseToken()
                        }
                    } catch (e: Exception) {
                        // Handle error dengan response custom
                        return@addInterceptor Response.Builder()
                            .request(request)
                            .protocol(okhttp3.Protocol.HTTP_1_1)
                            .code(401)
                            .message("Authentication failed: ${e.message}")
                            .body("{ \"error\": \"${e.message}\" }".toResponseBody())
                            .build()
                    }

                    // Membuat request dengan token
                    val newRequest = request.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()

                    // Lanjutkan dengan request baru
                    chain.proceed(newRequest)
                }
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
