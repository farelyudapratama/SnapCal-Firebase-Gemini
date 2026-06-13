package com.yuch.snapcalfirebasegemini.data.repository

import android.util.Log
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.ProfileRequest
import com.yuch.snapcalfirebasegemini.data.api.response.UserPreferences
import com.yuch.snapcalfirebasegemini.domain.result.AppResult

private const val ERROR_PROFILE_NOT_FOUND = "PROFILE_NOT_FOUND"

class ProfileRepository(private val apiService: ApiService) {

    suspend fun getProfile(): AppResult<UserPreferences> {
        return try {
            Log.d("ProfileRepository", "Fetching profile from backend...")
            val response = apiService.getProfile()

            if (response.isSuccessful) {
                val apiResponse: ApiResponse<UserPreferences>? = response.body()
                Log.d("ProfileRepository", "Profile response: ${apiResponse?.status}")

                if (apiResponse?.status == "success" && apiResponse.data != null) {
                    Log.d("ProfileRepository", "Profile data found: ${apiResponse.data}")
                    AppResult.Success(apiResponse.data, apiResponse.message)
                } else {
                    Log.w("ProfileRepository", "Profile not found or empty response")
                    AppResult.Error(
                        message = "Profile not found",
                        code = response.code(),
                        errorCode = ERROR_PROFILE_NOT_FOUND
                    )
                }
            } else {
                when (response.code()) {
                    404 -> {
                        Log.w("ProfileRepository", "Profile not found (404)")
                        AppResult.Error(
                            message = "Profile not found",
                            code = response.code(),
                            errorCode = ERROR_PROFILE_NOT_FOUND
                        )
                    }
                    else -> {
                        Log.e("ProfileRepository", "Failed to fetch profile: ${response.code()}")
                        AppResult.Error(
                            message = "Failed to fetch profile: ${response.message()}",
                            code = response.code()
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Unexpected error: ${e.message}")
            AppResult.Error(
                message = e.message ?: "Failed to fetch profile",
                cause = e
            )
        }
    }

    suspend fun updateProfile(profileRequest: ProfileRequest): AppResult<UserPreferences> {
        return try {
            Log.d("ProfileRepository", "Updating profile...")
            val response = apiService.postProfile(profileRequest)

            if (response.isSuccessful) {
                val apiResponse: ApiResponse<UserPreferences>? = response.body()

                if (apiResponse?.status == "success" && apiResponse.data != null) {
                    Log.d("ProfileRepository", "Profile updated successfully")
                    AppResult.Success(apiResponse.data, apiResponse.message)
                } else {
                    AppResult.Error(
                        message = apiResponse?.message ?: "Failed to update profile",
                        code = response.code()
                    )
                }
            } else {
                Log.e("ProfileRepository", "Failed to update profile: ${response.code()}")
                AppResult.Error(
                    message = "Failed to update profile: ${response.message()}",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Unexpected error during update: ${e.message}")
            AppResult.Error(
                message = e.message ?: "Failed to update profile",
                cause = e
            )
        }
    }
}
