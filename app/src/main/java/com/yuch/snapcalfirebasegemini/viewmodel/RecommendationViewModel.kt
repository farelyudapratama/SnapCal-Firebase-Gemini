package com.yuch.snapcalfirebasegemini.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.BuildConfig
import com.yuch.snapcalfirebasegemini.data.api.response.RecommendationData
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.domain.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "RecommendationViewModel"

sealed class RecommendationState {
    data object Loading : RecommendationState()
    data object Initial : RecommendationState()
    data class Success(val data: RecommendationData) : RecommendationState()
    data class Error(val message: String) : RecommendationState()
}

class RecommendationViewModel(
    private val apiRepository: ApiRepository
) : ViewModel() {

    private val _state = MutableStateFlow<RecommendationState>(RecommendationState.Initial)
    val state: StateFlow<RecommendationState> = _state.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        if (BuildConfig.DEBUG) Log.d(TAG, "ViewModel initialized")
    }

    fun loadRecommendations(mealType: String, refresh: Boolean = false) {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadRecommendations called - mealType: $mealType, refresh: $refresh")

        _state.value = RecommendationState.Loading

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                if (!isNetworkAvailable()) {
                    if (BuildConfig.DEBUG) Log.w(TAG, "No internet connection detected")
                    val errorMsg = "No internet connection"

                    _state.value = RecommendationState.Error(errorMsg)

                    _errorMessage.value = errorMsg
                    _isLoading.value = false
                    return@launch
                }

                if (BuildConfig.DEBUG) Log.d(TAG, "Calling API service for recommendations - mealType: $mealType, refresh: $refresh")

                when (val result = apiRepository.getRecommendation(mealType, refresh)) {
                    is AppResult.Success -> {
                        if (BuildConfig.DEBUG) Log.d(TAG, "Recommendations loaded successfully")
                        if (BuildConfig.DEBUG) Log.d(TAG, "Recommendations count: ${result.data.recommendations.size}")

                        _state.value = RecommendationState.Success(result.data)
                    }
                    is AppResult.Error -> {
                        val errorMsg = when (result.code) {
                        401 -> "Authentication failed. Please login again."
                        400 -> "Bad request. Please check your profile data is complete."
                        404 -> "Recommendations not found"
                        500 -> "Server error. Please try again later."
                            else -> result.message
                        }

                        _state.value = RecommendationState.Error(errorMsg)
                        _errorMessage.value = errorMsg

                        if (BuildConfig.DEBUG) Log.e(TAG, "Error: $errorMsg")
                    }
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Exception during API call", e)

                val errorMsg = when (e) {
                    is java.net.SocketTimeoutException -> "Request timeout. Please try again."
                    is java.net.UnknownHostException -> "No internet connection"
                    else -> e.message ?: "Unknown error occurred"
                }

                _state.value = RecommendationState.Error(errorMsg)

                _errorMessage.value = errorMsg

                if (BuildConfig.DEBUG) Log.e(TAG, "Exception: ${e.javaClass.simpleName} - $errorMsg")
            } finally {
                _isLoading.value = false
                if (BuildConfig.DEBUG) Log.d(TAG, "Set loading state to false")
                if (BuildConfig.DEBUG) Log.d(TAG, "Final state: ${_state.value}")
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return true
    }
}
