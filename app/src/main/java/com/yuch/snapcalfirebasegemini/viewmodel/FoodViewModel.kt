package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import com.yuch.snapcalfirebasegemini.data.model.UpdateFoodData
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

    private val _editResult = MutableStateFlow<ApiResponse<FoodItem>>(ApiResponse("error", "error", null))
    val editResult = _editResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess = _uploadSuccess.asStateFlow()

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

    // Upload Food data
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
                val nutritionJson = Gson().toJson(
                    mapOf(
                        "calories" to foodData.calories,
                        "carbs" to foodData.carbs,
                        "protein" to foodData.protein,
                        "totalFat" to foodData.totalFat,
                        "saturatedFat" to foodData.saturatedFat,
                        "fiber" to foodData.fiber,
                        "sugar" to foodData.sugar
                    )
                )
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

//    TODO CEK MY MODEL TFLITE
//    fun analyzeWithTFLite(imagePath: String) {
//    viewModelScope.launch {
//        _isLoading.value = true
//
//        try {
//            val bitmap = BitmapFactory.decodeFile(imagePath)
//            val result = tfliteClassifier.classify(bitmap) // Fungsi untuk menjalankan model
//
//            analysisResult.value = ResultState.Success(
//                AnalysisData(
//                    foodName = result.foodName,
//                    calories = result.calories,
//                    carbs = result.carbs,
//                    protein = result.protein,
//                    totalFat = result.totalFat,
//                    saturatedFat = result.saturatedFat,
//                    fiber = result.fiber,
//                    sugar = result.sugar
//                )
//            )
//        } catch (e: Exception) {
//            _errorMessage.value = "Error analyzing with TFLite: ${e.message}"
//        } finally {
//            _isLoading.value = false
//        }
//    }
//}

    // TODO Update Food data
    fun updateFood(
        foodId: String, imagePath: String?, foodData: UpdateFoodData?
    ) {
        if (foodData?.mealType == null) {
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

                val foodNamePart =
                    foodData.foodName?.toRequestBody("text/plain".toMediaTypeOrNull())
                val mealTypePart = foodData.mealType.toRequestBody("text/plain".toMediaTypeOrNull())

                val nutritionJson = Gson().toJson(
                    mapOf(
                        "calories" to foodData.calories,
                        "carbs" to foodData.carbs,
                        "protein" to foodData.protein,
                        "totalFat" to foodData.totalFat,
                        "saturatedFat" to foodData.saturatedFat,
                        "fiber" to foodData.fiber,
                        "sugar" to foodData.sugar
                    )
                )
                val nutritionPart = nutritionJson.toRequestBody("application/json".toMediaTypeOrNull())

                val response = apiService.updateFood(
                    id = foodId,
                    foodName = foodNamePart,
                    mealType = mealTypePart,
                    nutritionData = nutritionPart,
                    image = imagePart
                )

                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        when (apiResponse.status) {
                            "success" -> {
                                _editResult.value = ApiResponse(
                                    status = "success",
                                    message = "Food updated successfully",
                                    data = apiResponse.data
                                )
                                _successMessage.value = "Food updated successfully."
                            }
                            else -> {
                                _errorMessage.value = "[Code: ${response.code()}] ${apiResponse.message}"
                            }
                        }
                    } ?: run {
                        _errorMessage.value = "[Code: ${response.code()}] Empty response from server"
                    }
                } else {
                    handleErrorResponse(response)
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleErrorResponse(response: Response<ApiResponse<FoodItem>>) {
        try {
            val errorBody = response.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ApiResponse::class.java)
            _errorMessage.value = "[Code: ${response.code()}] ${errorResponse.message}"
        } catch (e: Exception) {
            _errorMessage.value = "[Code: ${response.code()}] ${response.message()}"
        }
    }


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
                            _uploadSuccess.value = true
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
        _uploadSuccess.value = false
        _errorMessage.value = null
    }
}