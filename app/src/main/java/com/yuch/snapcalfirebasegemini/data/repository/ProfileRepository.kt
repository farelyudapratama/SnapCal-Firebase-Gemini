package com.yuch.snapcalfirebasegemini.data.repository

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
            val response = apiService.getProfile()

            if (response.isSuccessful) {
                val apiResponse: ApiResponse<UserPreferences>? = response.body()
                return apiResponse?.data ?: throw Exception("Profile data is null in the response.")
            } else {
                if (response.code() == 404) {
                    throw ProfileNotFoundException()
                }
                throw HttpException(response)
            }
        } catch (e: HttpException) {
            throw e
        } catch (e: IOException) {
            throw IOException("Network error occurred while fetching profile.", e)
        } catch (e: Exception) {
            throw Exception("An unexpected error occurred: ${e.message}", e)
        }
    }

    suspend fun saveProfile(profileRequest: ProfileRequest): UserPreferences {
        try {
            val response = apiService.postProfile(profileRequest)

            if (response.isSuccessful) {
                val apiResponse: ApiResponse<UserPreferences>? = response.body()
                return apiResponse?.data ?: throw Exception("Profile data is null in the response after saving.")
            } else {
                throw HttpException(response)
            }
        } catch (e: IOException) {
            throw IOException("Network error occurred while saving profile.", e)
        } catch (e: Exception) {
            throw Exception("An unexpected error occurred while saving profile: ${e.message}", e)
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