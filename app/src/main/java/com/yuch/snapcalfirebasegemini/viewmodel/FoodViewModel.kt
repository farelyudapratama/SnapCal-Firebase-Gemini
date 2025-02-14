package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Response
import com.yuch.snapcalfirebasegemini.utils.ImageUtils
import java.io.IOException

class FoodViewModel(
    private val apiService: ApiService = ApiConfig.getApiService()
) : ViewModel() {

    private val _analysisResult = MutableStateFlow<ApiResponse<AnalyzeResult>>(ApiResponse("error", "error", null))
    val analysisResult = _analysisResult.asStateFlow()

    private val _uploadResult = MutableStateFlow<ApiResponse<Food>>(ApiResponse("error", "error", null))
    val uploadResult = _uploadResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    fun clearErrorMessage() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun analyzeImage(imagePath: String, service: String) {
        _analysisResult.value = ApiResponse("loading", "loading", null)
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Gunakan ImageHelper untuk validasi gambar
                val validationError = ImageUtils.validateImageFile(imagePath)
                if (validationError != null) throw Exception(validationError)

                _isLoading.value = true

                // Gunakan ImageHelper untuk menyiapkan gambar
                val (imagePart, mimeType) = ImageUtils.prepareImageForAnalyze(imagePath)
                val servicePart = service.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.analyzeFood(imagePart, servicePart)
                handleResponse(response)

            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // TODO Upload Food data
    fun uploadFood(imagePath: String?, foodData: EditableFoodData) {
        if (foodData.mealType == null) {
            _errorMessage.value = "Please select a meal type"
            return
        }
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val imagePart = imagePath?.let {
                    val validationError = ImageUtils.validateImageFile(it)
                    if (validationError != null) throw Exception(validationError)
                    ImageUtils.prepareImageForUpload(it).first
                }

                val foodNamePart = foodData.foodName.toRequestBody("text/plain".toMediaTypeOrNull())
                val mealTypePart = foodData.mealType!!.toRequestBody("text/plain".toMediaTypeOrNull())
                val nutritionJson = """
                    {
                        "calories": ${foodData.calories},
                        "carbs": ${foodData.carbs},
                        "protein": ${foodData.protein},
                        "totalFat": ${foodData.totalFat},
                        "saturatedFat": ${foodData.saturatedFat},
                        "fiber": ${foodData.fiber},
                        "sugar": ${foodData.sugar}
                    }
                """.trimIndent()
                val nutritionPart = nutritionJson.toRequestBody("application/json".toMediaTypeOrNull())

                val response = apiService.uploadFood(imagePart, foodNamePart, mealTypePart, nutritionPart)

                handleFoodResponse(response)

            } catch (e: Exception) {
                handleError(e)
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
//                    "success" -> {
//                        _analysisResult.value = apiResponse
//                        _errorMessage.value = null
//                    }
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

    private fun <T> handleFoodResponse(response: Response<ApiResponse<T>>) {
        if (response.isSuccessful) {
            response.body()?.let {
                _errorMessage.value = null
                _successMessage.value = it.message
            } ?: run {
                _errorMessage.value = "Empty response from server"
            }
        } else {
            _errorMessage.value = "HTTP Error: ${response.code()}, ${response.message()}"
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
}