package com.yuch.snapcalfirebasegemini.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.RecommendationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "RecommendationViewModel"

sealed class RecommendationState {
    object Loading : RecommendationState()
    object Initial : RecommendationState()
    data class Success(val data: RecommendationData) : RecommendationState()
    data class Error(val message: String) : RecommendationState()
}

class RecommendationViewModel(
    private val apiService: ApiService = ApiConfig.getApiService()
) : ViewModel() {

    private val _state = MutableStateFlow<RecommendationState>(RecommendationState.Initial)
    val state: StateFlow<RecommendationState> = _state.asStateFlow()

    private val _recommendationResult = MutableStateFlow<ApiResponse<RecommendationData>?>(null)
    val recommendationResult = _recommendationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        Log.d(TAG, "ViewModel initialized")
    }

    fun loadRecommendations(mealType: String, refresh: Boolean = false) {
        Log.d(TAG, "loadRecommendations called - mealType: $mealType, refresh: $refresh")

        _state.value = RecommendationState.Loading

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                if (!isNetworkAvailable()) {
                    Log.w(TAG, "No internet connection detected")
                    val errorMsg = "No internet connection"

                    _state.value = RecommendationState.Error(errorMsg)

                    _errorMessage.value = errorMsg
                    _recommendationResult.value = ApiResponse("error", errorMsg)
                    _isLoading.value = false
                    return@launch
                }

                Log.d(TAG, "Calling API service for recommendations - mealType: $mealType, refresh: $refresh")
                val response = apiService.getRecommendation(mealType, refresh)
                Log.d(TAG, "API response received - Code: ${response.code()}, Message: ${response.message()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d(TAG, "Response is successful, body null: ${body == null}")

                    if (body != null) {
                        Log.d(TAG, "Response body - status: ${body.status}, message: ${body.message}")
                        Log.d(TAG, "Response data null: ${body.data == null}")

                        if (body.data != null) {
                            Log.d(TAG, "Recommendations count: ${body.data.recommendations.size}")
                        }
                    }

                    if (body?.status == "success" && body.data != null) {
                        Log.d(TAG, "Recommendations loaded successfully")

                        _state.value = RecommendationState.Success(body.data)
                        _recommendationResult.value = body
                    } else {
                        val errorMsg = body?.message ?: "Failed to get recommendations"
                        Log.w(TAG, "Failed to load recommendations - Error: $errorMsg")

                        _state.value = RecommendationState.Error(errorMsg)

                        _errorMessage.value = errorMsg
                        _recommendationResult.value = ApiResponse("error", errorMsg)
                    }
                } else {
                    val errorCode = response.code()
                    Log.e(TAG, "API call failed - HTTP status code: $errorCode")

                    val errorMsg = when (errorCode) {
                        401 -> "Authentication failed. Please login again."
                        400 -> "Bad request. Please check your profile data is complete."
                        404 -> "Recommendations not found"
                        500 -> "Server error. Please try again later."
                        else -> "Network error: ${response.code()} ${response.message()}"
                    }

                    _state.value = RecommendationState.Error(errorMsg)
                    _errorMessage.value = errorMsg
                    _recommendationResult.value = ApiResponse("error", errorMsg)

                    Log.e(TAG, "Error: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during API call", e)

                val errorMsg = when (e) {
                    is java.net.SocketTimeoutException -> "Request timeout. Please try again."
                    is java.net.UnknownHostException -> "No internet connection"
                    else -> e.message ?: "Unknown error occurred"
                }

                _state.value = RecommendationState.Error(errorMsg)

                _errorMessage.value = errorMsg
                _recommendationResult.value = ApiResponse("error", errorMsg)

                Log.e(TAG, "Exception: ${e.javaClass.simpleName} - $errorMsg")
            } finally {
                _isLoading.value = false
                Log.d(TAG, "Set loading state to false")
                Log.d(TAG, "Final state: ${_state.value}")
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return true
    }
}