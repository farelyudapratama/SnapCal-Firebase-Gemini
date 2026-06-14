package com.yuch.snapcalfirebasegemini.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.FoodDetectionByMyModelResult
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionEstimateRequest
import com.yuch.snapcalfirebasegemini.data.mapper.MyModelAnalysisResult
import com.yuch.snapcalfirebasegemini.data.mapper.toMyModelAnalysisResult
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.domain.result.AppResult
import com.yuch.snapcalfirebasegemini.utils.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

class FoodViewModel(
    private val repository: ApiRepository
) : ViewModel() {

    private val _analysisResult = MutableStateFlow<AnalyzeResult?>(null)
    val analysisResult = _analysisResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    private val _yoloDetectionResult = MutableStateFlow<List<FoodDetectionByMyModelResult>?>(null)
    val yoloDetectionResult = _yoloDetectionResult.asStateFlow()

    fun clearErrorMessage() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun analyzeImage(imagePath: String, service: String) {
        _analysisResult.value = null
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

                val result = repository.analyzeFood(imagePart, servicePart)
                handleAnalyzeResult(result)

            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Analyze food using custom model saat ini YOLO
    fun analyzeFoodByMyModel(imagePath: String) {
        _errorMessage.value = null
        _yoloDetectionResult.value = null // Clear previous detections

        viewModelScope.launch {
            try {
                Log.d("CustomModel", "Start analyzing image at path: $imagePath")

                ImageUtils.validateImageFile(imagePath)?.let {
                    throw Exception(it)
                }

                _isLoading.value = true
                val (imagePart, mimeType) = ImageUtils.prepareImageForAnalyze(imagePath)
                Log.d("CustomModel", "Image prepared. MimeType: $mimeType")

                val result = repository.analyzeFoodByMyModel(imagePart)
                if (result is AppResult.Error) {
                    _errorMessage.value = result.toDisplayMessage()
                    return@launch
                }

                when (val analysis = (result as AppResult.Success).data.toMyModelAnalysisResult()) {
                    is MyModelAnalysisResult.YoloDetections -> {
                        _yoloDetectionResult.value = analysis.detections
                        Log.d("CustomModel", "YOLO detected ${analysis.detections.size} items")
                    }
                    is MyModelAnalysisResult.AiFallback -> {
                        _analysisResult.value = analysis.result
                        _successMessage.value = if (analysis.isYoloFallback) {
                            "YOLO model failed to detect food. Analysis performed using Gemini AI."
                        } else {
                            "Analysis completed using external AI."
                        }
                        Log.d("CustomModel", "AI analysis parsed successfully")
                    }
                    is MyModelAnalysisResult.Error -> {
                        _yoloDetectionResult.value = emptyList()
                        _errorMessage.value = analysis.message
                    }
                }

                Log.d("CustomModel", "Analysis finished")
            } catch (e: Exception) {
                Log.e("CustomModel", "Exception during analysis", e)
                handleError(e)
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
                Log.d("CustomModel", "Analysis finished")
            }
        }
    }

    // Add method to estimate nutrition by food name
    fun estimateNutritionByName(foodName: String, description: String? = null) {
        _analysisResult.value = null
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val request = NutritionEstimateRequest(foodName, description)
                val result = repository.estimateNutritionByName(request)
                handleAnalyzeResult(result)

            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Clear YOLO detection results
    fun clearYoloDetections() {
        _yoloDetectionResult.value = null
    }

    private fun handleAnalyzeResult(result: AppResult<AnalyzeResult>) {
        when (result) {
            is AppResult.Success -> {
                _analysisResult.value = result.data
                _errorMessage.value = null
            }
            is AppResult.Error -> {
                _errorMessage.value = result.toDisplayMessage()
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

    private fun AppResult.Error.toDisplayMessage(): String =
        code?.let { "[Code: $it] $message" } ?: message

    fun clearData() {
        _analysisResult.value = null
        _isLoading.value = false
        _errorMessage.value = null
        _successMessage.value = null
        _yoloDetectionResult.value = null
    }
}
