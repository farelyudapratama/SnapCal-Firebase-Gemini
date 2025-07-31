package com.yuch.snapcalfirebasegemini.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeByMyModelResponse
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import com.yuch.snapcalfirebasegemini.data.api.response.FoodDetectionByMyModelResult
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionEstimateRequest
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
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import com.yuch.snapcalfirebasegemini.ml.ModelTeachable
import com.yuch.snapcalfirebasegemini.utils.normalizeDecimal
import org.json.JSONObject

class FoodViewModel(
    private val apiService: ApiService = ApiConfig.getApiService()
) : ViewModel() {

    private val _analysisResult = MutableStateFlow<ApiResponse<AnalyzeResult>>(ApiResponse("error", "error", null))
    val analysisResult = _analysisResult.asStateFlow()

    private val _yoloAnalysisResult = MutableStateFlow<ApiResponse<AnalyzeByMyModelResponse>>(ApiResponse("error", "error", null))
    val yoloAnalysisResult = _yoloAnalysisResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess = _uploadSuccess.asStateFlow()

    private val _yoloDetectionResult = MutableStateFlow<List<FoodDetectionByMyModelResult>?>(null)
    val yoloDetectionResult = _yoloDetectionResult.asStateFlow()

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

                val response = apiService.analyzeFoodByMyModel(imagePart)
                Log.d("CustomModel", "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CustomModel", "Raw error body: $errorBody")
                    val message = try {
                        JSONObject(errorBody ?: "{}").optString("message", "Unknown server error")
                    } catch (e: Exception) {
                        "Request failed with code ${response.code()}"
                    }
                    _errorMessage.value = "[Code: ${response.code()}] $message"
                    return@launch
                }

                val body = response.body()
                if (body == null) {
                    _errorMessage.value = "[Code: ${response.code()}] Empty response from server"
                    return@launch
                }

                Log.d("CustomModel", "API Status: ${body.status}, Message: ${body.message}")

                if (body.status != "success") {
                    _errorMessage.value = "[Code: ${response.code()}] ${body.message}"
                    return@launch
                }

                val rawJson = Gson().toJson(body.data)
                Log.d("CustomModel", "Raw JSON response: $rawJson")

                when (body.message) {
                    "Makanan berhasil dideteksi oleh model YoLo" -> {
                        try {
                            val detectionResponse = Gson().fromJson(rawJson, AnalyzeByMyModelResponse::class.java)
                            Log.d("CustomModel", "Parsed YOLO detection response: $detectionResponse")

                            val detections = detectionResponse?.detections
                            if (detections.isNullOrEmpty()) {
                                Log.w("CustomModel", "No detections found")
                                _yoloDetectionResult.value = emptyList()
                                Log.d("CustomModel", "YOLO detected 0 items")
                            } else {
                                _yoloDetectionResult.value = detections
                                Log.d("CustomModel", "YOLO detected ${detections.size} items")
                            }

                            _yoloAnalysisResult.value = ApiResponse("success", body.message, detectionResponse)
                        } catch (e: Exception) {
                            Log.e("CustomModel", "Failed to parse YOLO detection result", e)
                            _yoloDetectionResult.value = emptyList()
                            _errorMessage.value = "Failed to parse YOLO detection result"
                        }
                    }

                    "Image analyzed successfully" -> {
                        try {
                            val aiResult = Gson().fromJson(rawJson, AnalyzeResult::class.java)
                            _analysisResult.value = ApiResponse("success", body.message, aiResult)
                        } catch (e: Exception) {
                            Log.e("CustomModel", "Failed to parse AI result", e)
                            _errorMessage.value = "Failed to parse AI analysis result"
                        }
                    }

                    "Model tidak mendeteksi, hasil diperoleh dari AI eksternal" -> {
                        try {
                            val aiResult = Gson().fromJson(rawJson, AnalyzeResult::class.java)
                            _analysisResult.value = ApiResponse("success", body.message, aiResult)

                            // Show notification when YOLO failed and fallback to AI
                            _successMessage.value = "Model YOLO gagal mendeteksi makanan. Analisis dilakukan menggunakan Gemini AI."
                        } catch (e: Exception) {
                            Log.e("CustomModel", "Failed to parse AI fallback result", e)
                            _errorMessage.value = "Failed to parse AI analysis result"
                        }
                    }

                    else -> {
                        Log.w("CustomModel", "Unexpected message: ${body.message}")
                        // Try to parse as AI result for any other success message
                        try {
                            val aiResult = Gson().fromJson(rawJson, AnalyzeResult::class.java)
                            _analysisResult.value = ApiResponse("success", body.message, aiResult)

                            // Show fallback notification for unexpected messages
                            if (body.message.contains("tidak mendeteksi", ignoreCase = true) ||
                                body.message.contains("eksternal", ignoreCase = true)) {
                                _successMessage.value = "Model YOLO gagal mendeteksi makanan. Analisis dilakukan menggunakan Gemini AI."
                            }
                        } catch (e: Exception) {
                            Log.e("CustomModel", "Failed to parse unknown response type", e)
                            _errorMessage.value = "Unexpected response format: ${body.message}"
                        }
                    }
                }

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

    // Upload Food data
    fun uploadFood(imagePath: String?, foodData: EditableFoodData) {
        if (foodData.mealType == null) {
            _errorMessage.value = "Please select a meal type"
            return
        }
        _isLoading.value = true
        _errorMessage.value = null

        // Always normalize all nutrition fields and weight before upload
        val safeFoodData = foodData.copy(
            calories = foodData.calories.normalizeDecimal(),
            carbs = foodData.carbs.normalizeDecimal(),
            protein = foodData.protein.normalizeDecimal(),
            totalFat = foodData.totalFat.normalizeDecimal(),
            saturatedFat = foodData.saturatedFat.normalizeDecimal(),
            fiber = foodData.fiber.normalizeDecimal(),
            sugar = foodData.sugar.normalizeDecimal(),
            weightInGrams = foodData.weightInGrams.normalizeDecimal()
        )

        viewModelScope.launch {
            try {
                val imagePart = imagePath?.let {
                    val validationError = ImageUtils.validateImageFile(it)
                    if (validationError != null) throw Exception(validationError)
                    ImageUtils.prepareImageForUpload(it).first
                }

                val foodNamePart = safeFoodData.foodName.toRequestBody("text/plain".toMediaTypeOrNull())
                val mealTypePart = safeFoodData.mealType!!.toRequestBody("text/plain".toMediaTypeOrNull())
                val weightPart = safeFoodData.weightInGrams.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val nutritionJson = Gson().toJson(
                    mapOf(
                        "calories" to safeFoodData.calories,
                        "carbs" to safeFoodData.carbs,
                        "protein" to safeFoodData.protein,
                        "totalFat" to safeFoodData.totalFat,
                        "saturatedFat" to safeFoodData.saturatedFat,
                        "fiber" to safeFoodData.fiber,
                        "sugar" to safeFoodData.sugar
                    )
                )
                val nutritionPart = nutritionJson.toRequestBody("application/json".toMediaTypeOrNull())

                val response = apiService.uploadFood(imagePart, foodNamePart, mealTypePart, weightPart, nutritionPart)

                handleFoodResponse(response)
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(224 * 224)
        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)

        for (pixel in intValues) {
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        return byteBuffer
    }

    //    TODO CEK MY MODEL TFLITE
    fun analyzeWithTFLite(imagePath: String, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Load gambar sebagai Bitmap
                val bitmap = BitmapFactory.decodeFile(imagePath)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

                // Konversi ke ByteBuffer untuk model
                val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)

                // Load model TFLite
                val model = ModelTeachable.newInstance(context)

                // Buat input untuk model
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)

                // Jalankan model
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                // Ambil hasil klasifikasi
                val scores = outputFeature0.floatArray
                val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: -1

                // List label yang digunakan saat training di Teachable Machine
                val labels = listOf("ayam_geprek", "bakso", "ketoprak", "lontong_sayur", "martabak_manis", "martabak_telur", "mie_goreng", "nasi_goreng", "nasi_padang", "nasi_uduk", "pecel_lele", "pempek", "rawon", "rendang", "siomay", "sate", "soto", "tahu_gejrot")  // Ganti dengan label yang sesuai

                if (maxIndex != -1 && maxIndex < labels.size) {
                    val detectedFood = labels[maxIndex]
                    val confidence = scores[maxIndex]

                    Log.d("TFLite", "Detected: $detectedFood ($confidence)")

                    _analysisResult.value = ApiResponse(
                        "success",
                        "Detected Food: $detectedFood",
                        AnalyzeResult(
                            foodName = detectedFood,
                            calories = 100.0,  // Bisa dikembangkan Ntar  moga semangat
                            carbs = 20.0,
                            protein = 5.0,
                            totalFat = 3.0,
                            saturatedFat = 1.0,
                            fiber = 2.0,
                            sugar = 10.0
                        )
                    )
                } else {
                    _errorMessage.value = "No object detected."
                }

                // Tutup model
                model.close()

            } catch (e: Exception) {
                _errorMessage.value = "Error analyzing with TFLite: ${e.message}"
                Log.e("FoodViewModel", "Error analyzing with TFLite", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update Food data
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
                val weightPart = foodData.weightInGrams.toString().toRequestBody("text/plain".toMediaTypeOrNull())

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
                    weightInGrams = weightPart,
                    nutritionData = nutritionPart,
                    image = imagePart
                )

                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        when (apiResponse.status) {
                            "success" -> {
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


    // Add method to estimate nutrition by food name
    fun estimateNutritionByName(foodName: String, description: String? = null) {
        _analysisResult.value = ApiResponse("loading", "Estimating nutrition...", null)
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val request = NutritionEstimateRequest(foodName, description)
                val response = apiService.estimateNutritionByName(request)
                handleResponse(response)

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
