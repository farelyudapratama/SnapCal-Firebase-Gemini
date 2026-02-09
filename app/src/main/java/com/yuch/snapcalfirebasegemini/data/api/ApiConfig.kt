package com.yuch.snapcalfirebasegemini.data.api

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.tasks.await
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.TimeUnit

class ApiConfig {

    companion object {

        private const val BASE_URL = ""
        
        // Token caching untuk mengurangi panggilan Firebase
        @Volatile
        private var cachedToken: String? = null
        @Volatile
        private var tokenExpireTime: Long = 0
        
        // Token valid for 55 minutes (Firebase token expires in 1 hour)
        private const val TOKEN_CACHE_DURATION_MS = 55 * 60 * 1000L

        /**
         * Get Firebase token with caching
         * Token di-cache selama 55 menit untuk mengurangi overhead
         */
        private suspend fun getFirebaseToken(forceRefresh: Boolean = false): String {
            val currentTime = System.currentTimeMillis()
            
            // Return cached token if still valid and not forcing refresh
            if (!forceRefresh && cachedToken != null && currentTime < tokenExpireTime) {
                return cachedToken!!
            }
            
            val user = FirebaseAuth.getInstance().currentUser
                ?: throw Exception("User not authenticated")

            return try {
                withContext(Dispatchers.IO) {
                    kotlinx.coroutines.withTimeout(10000) {
                        val token = user.getIdToken(forceRefresh).await()?.token
                            ?: throw Exception("Token is null")
                        
                        // Cache the token
                        cachedToken = token
                        tokenExpireTime = currentTime + TOKEN_CACHE_DURATION_MS
                        
                        token
                    }
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                throw Exception("Token retrieval timeout after 10 seconds")
            } catch (e: Exception) {
                throw Exception("Token retrieval failed: ${e.message}")
            }
        }
        
        /**
         * Clear cached token (call on logout)
         */
        fun clearTokenCache() {
            cachedToken = null
            tokenExpireTime = 0
        }

        fun getApiService(): ApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .callTimeout(120, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val request = chain.request()

                    // runBlocking dengan Dispatchers.IO memastikan ini berjalan di background thread
                    // OkHttp interceptor sudah dipanggil di background thread oleh OkHttp
                    val token = try {
                        runBlocking(Dispatchers.IO) {
                            getFirebaseToken()
                        }
                    } catch (e: Exception) {
                        return@addInterceptor Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(401)
                            .message("Authentication failed: ${e.message}")
                            .body("{ \"error\": \"${e.message}\" }".toResponseBody())
                            .build()
                    }

                    val newRequest = request.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()

                    chain.proceed(newRequest)
                }
                // Authenticator untuk handle 401 dengan token refresh
                .authenticator { _, response ->
                    // Hanya retry sekali untuk menghindari infinite loop
                    if (response.request.header("Authorization-Retry") != null) {
                        return@authenticator null
                    }
                    
                    // Force refresh token dan retry request
                    val newToken = try {
                        runBlocking(Dispatchers.IO) {
                            getFirebaseToken(forceRefresh = true)
                        }
                    } catch (e: Exception) {
                        return@authenticator null
                    }
                    
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .header("Authorization-Retry", "true")
                        .build()
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

