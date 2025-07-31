package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.RecommendationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecommendationViewModel(
    private val apiService: ApiService = ApiConfig.getApiService()
) : ViewModel() {

    private val _recommendationResult = MutableStateFlow<ApiResponse<RecommendationData>?>(null)
    val recommendationResult = _recommendationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun loadRecommendations(mealType: String, refresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = apiService.getRecommendation(mealType, refresh)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        _recommendationResult.value = body
                    } else {
                        _errorMessage.value = body?.message ?: "Failed to get recommendations"
                        _recommendationResult.value = ApiResponse("error", body?.message ?: "Unknown error")
                    }
                } else {
                    val errorMsg = "Network error: ${response.code()} ${response.message()}"
                    _errorMessage.value = errorMsg
                    _recommendationResult.value = ApiResponse("error", errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _errorMessage.value = errorMsg
                _recommendationResult.value = ApiResponse("error", errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearData() {
        _recommendationResult.value = null
        _isLoading.value = false
        _errorMessage.value = null
    }
}