package com.yuch.snapcalfirebasegemini.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
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
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.ImageUploadRequest
import com.yuch.snapcalfirebasegemini.data.api.response.ProfileRequest
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
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import com.yuch.snapcalfirebasegemini.ml.ModelTeachable

class FoodViewModel(
    private val apiService: ApiService = ApiConfig.getApiService()
) : ViewModel() {

    private val _analysisResult = MutableStateFlow<ApiResponse<AnalyzeResult>>(ApiResponse("error", "error", null))
    val analysisResult = _analysisResult.asStateFlow()

    private val _customModelResult = MutableStateFlow<ApiResponse<AnalyzeByMyModelResponse>>(ApiResponse("idle", "Idle"))
    val customModelResult = _customModelResult.asStateFlow()

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

    // Analyze food using custom model with post base64
    fun analyzeFoodByMyModel(imagePath: String) {
        _customModelResult.value = ApiResponse("loading", "Loading")
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val validationError = ImageUtils.validateImageFile(imagePath)
                if (validationError != null) throw Exception(validationError)

                _isLoading.value = true

                val base64Image = ImageUtils.prepareImageForBase64(imagePath)
                val requestBody = ImageUploadRequest(base64Image)

                val response = apiService.analyzeFoodByMyModel(requestBody)

                if(response.isSuccessful){
                    response.body()?.let { apiResponse ->
                        when (apiResponse.status) {
                            "success" -> {
                                _customModelResult.value = ApiResponse(
                                    status = "success",
                                    message = "Food analyzed successfully",
                                    data = apiResponse.data
                                )
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
                    Log.d("CustomModel", "Response: ${response.body()}")
                } else {
                    _customModelResult.value = ApiResponse("error", "Request failed: ${response.code()}")
                }

            } catch (e: Exception) {
                handleError(e)
                _customModelResult.value = ApiResponse("error", e.message ?: "Unknown error")
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

                val response = apiService.uploadFood(imagePart, foodNamePart, mealTypePart, weightPart,nutritionPart)

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

    fun postProfileRequest(
        profileData: ProfileRequest
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = apiService.postProfile(profileData)

                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        when (apiResponse.status) {
                            "success" -> {
                                _successMessage.value = "Profile updated successfully."
                                _errorMessage.value = null
                            }
                            "error" -> {
                                _errorMessage.value = "[Code: ${response.code()}] ${apiResponse.message}"
                            }
                        }
                    } ?: run {
                        _errorMessage.value = "[Code: ${response.code()}] Empty response from server"
                    }
                } else {
                    if (response.isSuccessful) {
                        response.body()
                            ?.let { apiResponse ->
                                _errorMessage.value = "[Code: ${response.code()}] ${apiResponse.message}"
                            } ?: run {
                                _errorMessage.value = "[Code: ${response.code()}] Empty response from server"
                            }
                    } else if (response.errorBody() != null) {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ApiResponse::class.java)
                            _errorMessage.value = "[Code: ${response.code()}] ${errorResponse.message}"
                        } catch (e: Exception) {
                            _errorMessage.value = "[Code: ${response.code()}] ${response.message()}"
                        }
                    } else {
                        _errorMessage.value = "[Code: ${response.code()}] ${response.message()}"
                    }
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