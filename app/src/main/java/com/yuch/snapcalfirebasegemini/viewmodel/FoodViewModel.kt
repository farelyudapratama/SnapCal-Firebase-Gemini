package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Response
import com.yuch.snapcalfirebasegemini.utils.ImageUtils
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException

class FoodViewModel(
    private val repository: ApiRepository
) : ViewModel() {

    private val _analysisResult = MutableStateFlow<ApiResponse<AnalyzeResult>?>(null)
    val analysisResult = _analysisResult.asStateFlow()

    private val _uploadResult = MutableStateFlow<ApiResponse<Food>?>(null)
    val uploadResult = _uploadResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess = _uploadSuccess.asStateFlow()

    private val _foodList = MutableStateFlow<List<Food>>(emptyList())
    val foodList: StateFlow<List<Food>> = _foodList

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    private val _isLoadingPage = MutableStateFlow(false)
    val isLoadingPage: StateFlow<Boolean> = _isLoadingPage

    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData

    init {
        fetchFood()
    }

    fun fetchFood() {
        if (_isLoadingPage.value || !_hasMoreData.value) return

        viewModelScope.launch {
            _isLoadingPage.value = true
            try {
                val response = repository.getAllFood(_currentPage.value)
                val newList = _foodList.value + (response.data?.items
                    ?: emptyList())
                _foodList.value = newList

                _hasMoreData.value = _currentPage.value < response.data!!.totalPages
                _currentPage.value += 1
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingPage.value = false
            }
        }
    }


    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun analyzeImage(imagePath: String, service: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _analysisResult.value = repository.analyzeImage(imagePath, service)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadFood(imagePath: String?, foodData: EditableFoodData) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _uploadResult.value = repository.uploadFood(imagePath, foodData)
                _uploadSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // TODO Update Food data

    private fun handleResponse(response: Response<ApiResponse<AnalyzeResult>>) {
        if (response.isSuccessful) {
            response.body()?.let { apiResponse ->
                when (apiResponse.status) {
                    "success" -> {
                        _analysisResult.value = apiResponse
                        _errorMessage.value = null
                    }
                    "error" -> {
                        val statusCode = response.code()
                        _errorMessage.value = "[Code: $statusCode] ${apiResponse.message}"
                    }
                }
            } ?: run {
                _errorMessage.value = "[Code: ${response.code()}] Empty response from server"
            }
        } else {
            try {
                // Parse error response from backend
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ApiResponse::class.java)
                _errorMessage.value = "[Code: ${response.code()}] ${errorResponse.message}"
            } catch (e: Exception) {
                _errorMessage.value = "[Code: ${response.code()}] ${response.message()}"
            }
        }
    }

    private fun handleFoodResponse(response: Response<ApiResponse<Food>>) {
        if (response.isSuccessful) {
            response.body()
                ?.let { apiResponse ->
                    when (apiResponse.status) {
                        "success" -> {
                            _uploadResult.value =
                                apiResponse
                            _errorMessage.value =
                                null
//                            _uploadSuccess.value = true
                        }

                        "error" -> {
                            val statusCode =
                                response.code()
                            _errorMessage.value =
                                "[Code: $statusCode] ${apiResponse.message}"
                        }
                    }
                }
                ?: run {
                    _errorMessage.value =
                        "[Code: ${response.code()}] Empty response from server"
                }
        } else {
            try {
                // Parse error response from backend
                val errorBody =
                    response.errorBody()
                        ?.string()
                val errorResponse =
                    Gson().fromJson(
                        errorBody,
                        ApiResponse::class.java
                    )
                _errorMessage.value =
                    "[Code: ${response.code()}] ${errorResponse.message}"
            } catch (e: Exception) {
                _errorMessage.value =
                    "[Code: ${response.code()}] ${response.message()}"
            }
        }
    }

    private fun handleError(exception: Exception) {
        when (exception) {
            is IOException -> _errorMessage.value = "Network error. Please check your internet connection."
            is HttpException -> {
                val errorBody = exception.response()?.errorBody()?.string() ?: "Unknown server error"
                _errorMessage.value = "Server error: $errorBody"
            }
            else -> _errorMessage.value = exception.message ?: "An unexpected error occurred"
        }
    }

    fun resetState() {
//        _uploadSuccess.value = false
        _errorMessage.value = null
    }
}