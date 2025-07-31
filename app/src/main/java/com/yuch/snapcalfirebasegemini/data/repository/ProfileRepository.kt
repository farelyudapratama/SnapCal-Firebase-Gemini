package com.yuch.snapcalfirebasegemini.data.repository

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.ProfileRequest
import com.yuch.snapcalfirebasegemini.data.api.response.UserPreferences
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel
import retrofit2.HttpException
import java.io.IOException

class ProfileNotFoundException(message: String = "User profile not found") : Exception(message)

class ProfileRepository(private val apiService: ApiService) {

    suspend fun getProfile(): UserPreferences {
        try {
            Log.d("ProfileRepository", "Fetching profile from backend...")
            val response = apiService.getProfile()

            if (response.isSuccessful) {
                val apiResponse: ApiResponse<UserPreferences>? = response.body()
                Log.d("ProfileRepository", "Profile response: ${apiResponse?.status}")

                if (apiResponse?.status == "success" && apiResponse.data != null) {
                    Log.d("ProfileRepository", "Profile data found: ${apiResponse.data}")
                    return apiResponse.data
                } else {
                    Log.w("ProfileRepository", "Profile not found or empty response")
                    throw ProfileNotFoundException("Profile not found")
                }
            } else {
                when (response.code()) {
                    404 -> {
                        Log.w("ProfileRepository", "Profile not found (404)")
                        throw ProfileNotFoundException("Profile not found")
                    }
                    else -> {
                        Log.e("ProfileRepository", "Failed to fetch profile: ${response.code()}")
                        throw Exception("Failed to fetch profile: ${response.message()}")
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("ProfileRepository", "Network error: ${e.message}")
            throw Exception("Network error. Please check your internet connection.")
        } catch (e: HttpException) {
            Log.e("ProfileRepository", "HTTP error: ${e.code()}")
            if (e.code() == 404) {
                throw ProfileNotFoundException("Profile not found")
            }
            throw Exception("Server error: ${e.message()}")
        } catch (e: ProfileNotFoundException) {
            throw e
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Unexpected error: ${e.message}")
            throw e
        }
    }

    suspend fun updateProfile(profileRequest: ProfileRequest): UserPreferences {
        try {
            Log.d("ProfileRepository", "Updating profile...")
            val response = apiService.postProfile(profileRequest)

            if (response.isSuccessful) {
                val apiResponse: ApiResponse<UserPreferences>? = response.body()

                if (apiResponse?.status == "success" && apiResponse.data != null) {
                    Log.d("ProfileRepository", "Profile updated successfully")
                    return apiResponse.data
                } else {
                    throw Exception("Failed to update profile")
                }
            } else {
                Log.e("ProfileRepository", "Failed to update profile: ${response.code()}")
                throw Exception("Failed to update profile: ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("ProfileRepository", "Network error during update: ${e.message}")
            throw Exception("Network error. Please check your internet connection.")
        } catch (e: HttpException) {
            Log.e("ProfileRepository", "HTTP error during update: ${e.code()}")
            throw Exception("Server error: ${e.message()}")
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Unexpected error during update: ${e.message}")
            throw e
        }
    }

    companion object {
        @Volatile
        private var instance: ProfileRepository? = null

        fun getInstance(apiService: ApiService): ProfileRepository =
            instance ?: synchronized(this) {
                instance ?: ProfileRepository(apiService).also { instance = it }
            }
    }
}

class ProfileViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val repository = ProfileRepository.getInstance(apiService)
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}