package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.FoodPage
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionData
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
    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess = _uploadSuccess.asStateFlow()
    // State untuk daftar makanan
    private val _foodList = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodList: StateFlow<List<FoodItem>> = _foodList

    // Pagination: halaman saat ini dan total halaman
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages

    // Flag apakah masih ada data yang harus dimuat
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData

    // State loading dan error
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // State untuk hasil analisis gambar
    private val _analysisResult = MutableStateFlow<ApiResponse<AnalyzeResult>?>(null)
    val analysisResult: StateFlow<ApiResponse<AnalyzeResult>?> = _analysisResult

    // State untuk hasil upload makanan
    private val _uploadResult = MutableStateFlow<ApiResponse<Food>?>(null)
    val uploadResult: StateFlow<ApiResponse<Food>?> = _uploadResult

    init {
        // Ambil data dari halaman pertama
        fetchFood(page = 1)
    }

    /**
     * Mengambil data makanan dari API berdasarkan halaman.
     * Jika halaman = 1, data akan direplace (refresh), sedangkan halaman > 1 akan diappend.
     */
    fun fetchFood(page: Int = _currentPage.value) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Coba ambil data dari API
                val response: ApiResponse<FoodPage> = repository.getAllFood(page)
                val fetchedItems = response.data?.items ?: emptyList()
                _foodList.value = if (page == 1) {
                    fetchedItems
                } else {
                    _foodList.value + fetchedItems
                }
                _currentPage.value = page
                _totalPages.value = response.data?.totalPages ?: 1
                _hasMoreData.value = _currentPage.value < _totalPages.value
            } catch (e: Exception) {
                // Jika terjadi error (misalnya tidak ada internet), ambil data dari cache
                _errorMessage.value = e.message
                val cachedData = repository.getCachedFoods()
                // Konversikan FoodEntity ke Food (harus Anda implementasikan fungsi mapping-nya)
                _foodList.value = cachedData.map{
                    FoodItem(
                        id = it.id,
                        userId = it.userId,
                        foodName = it.foodName,
                        mealType = it.mealType,
                        nutritionData = NutritionData(
                            calories = it.calories,
                            carbs = it.carbs,
                            protein = it.protein,
                            totalFat = it.totalFat,
                            saturatedFat = it.saturatedFat,
                            fiber = it.fiber,
                            sugar = it.sugar
                        ),
                        imageUrl = it.imageUrl,
                        createdAt = it.createdAt.toString()
                    )
                }
                // Set flag hasMoreData false jika data cache digunakan (atau Anda bisa logika lain)
                _hasMoreData.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }


    /**
     * Memuat halaman selanjutnya jika masih ada.
     */
    fun loadNextPage() {
        if (_currentPage.value < _totalPages.value && !_isLoading.value) {
            fetchFood(page = _currentPage.value + 1)
        }
    }

    /**
     * Refresh data dengan mengambil ulang dari halaman pertama.
     */
    fun refreshFood() {
        _foodList.value = emptyList()
        _currentPage.value = 1
        fetchFood(page = 1)
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