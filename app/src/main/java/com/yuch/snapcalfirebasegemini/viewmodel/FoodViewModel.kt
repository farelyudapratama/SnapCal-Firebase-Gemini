package com.yuch.snapcalfirebasegemini.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeByMyModelResponse
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.DailySummaryResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionData
import com.yuch.snapcalfirebasegemini.data.api.response.UserPreferences
import com.yuch.snapcalfirebasegemini.data.api.response.WeeklySummaryResponse
import com.yuch.snapcalfirebasegemini.data.model.UpdateFoodData
import com.yuch.snapcalfirebasegemini.data.repository.FoodRepository
import com.yuch.snapcalfirebasegemini.utils.formatDateFromLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel untuk menangani operasi terkait makanan
 * Menggabungkan fitur dari FoodViewModel dan GetFoodViewModel
 */
class FoodViewModel(
    private val repository: FoodRepository
) : ViewModel() {

    // Food list data
    private val _foodList = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodList: StateFlow<List<FoodItem>> = _foodList.asStateFlow()

    // Selected food detail
    private val _food = MutableStateFlow<FoodItem?>(null)
    val food: StateFlow<FoodItem?> = _food.asStateFlow()

    // Analysis result
    private val _analysisResult = MutableStateFlow<AnalyzeResult?>(null)
    val analysisResult = _analysisResult.asStateFlow()

    // Custom model analysis result
    private val _customModelResult = MutableStateFlow<AnalyzeByMyModelResponse?>(null)
    val customModelResult = _customModelResult.asStateFlow()

    // Upload result
    private val _uploadResult = MutableStateFlow<Food?>(null)
    val uploadResult = _uploadResult.asStateFlow()

    // Edit result
    private val _editResult = MutableStateFlow<FoodItem?>(null)
    val editResult = _editResult.asStateFlow()

    // Pagination
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()

    // State flags
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess = _uploadSuccess.asStateFlow()
    
    private val _imageDeletedMessage = MutableStateFlow<String?>(null)
    val imageDeletedMessage: StateFlow<String?> = _imageDeletedMessage.asStateFlow()

    // Daily summary
    private val _dailySummary = MutableStateFlow<DailySummaryResponse?>(null)
    val dailySummary: StateFlow<DailySummaryResponse?> = _dailySummary.asStateFlow()

    // Weekly summary
    private val _weeklySummary = MutableStateFlow<WeeklySummaryResponse?>(null)
    val weeklySummary: StateFlow<WeeklySummaryResponse?> = _weeklySummary.asStateFlow()

    // User preferences
    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()

    // Flag to indicate data was updated
    private val _dataUpdated = MutableStateFlow(false)
    val dataUpdated: StateFlow<Boolean> = _dataUpdated.asStateFlow()

    init {
        fetchFood(page = 1)
        fetchDailySummary()
        fetchWeeklySummary()
    }

    /**
     * Mengambil daftar makanan dengan pagination
     */
    fun fetchFood(page: Int = _currentPage.value) {
        viewModelScope.launch {
            Log.d("FoodViewModel", "Fetching food data for page: $page")
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.getAllFood(page).fold(
                    onSuccess = { foodPage ->
                        val fetchedItems = foodPage.items
                        // Jika halaman pertama, replace data; jika tidak, append data
                        _foodList.value = if (page == 1) {
                            fetchedItems
                        } else {
                            _foodList.value + fetchedItems
                        }
                        _currentPage.value = page
                        _totalPages.value = foodPage.totalPages
                        _hasMoreData.value = page < foodPage.totalPages
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message
                        // Jika ada error, coba ambil data dari cache
                        if (page == 1) {
                            val cachedData = repository.getCachedFoods()
                            if (cachedData.isNotEmpty()) {
                                _foodList.value = cachedData.map { entity ->
                                    FoodItem(
                                        id = entity.id,
                                        userId = entity.userId,
                                        foodName = entity.foodName,
                                        mealType = entity.mealType,
                                        weightInGrams = entity.weightInGrams.toString(),
                                        nutritionData = NutritionData(
                                            calories = entity.calories,
                                            carbs = entity.carbs,
                                            protein = entity.protein,
                                            totalFat = entity.totalFat,
                                            saturatedFat = entity.saturatedFat,
                                            fiber = entity.fiber,
                                            sugar = entity.sugar
                                        ),
                                        imageUrl = entity.imageUrl,
                                        createdAt = formatDateFromLong(entity.createdAt)
                                    )
                                }
                                _hasMoreData.value = false
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Memuat lebih banyak data (pagination)
     */
    fun loadMore() {
        if (!_isLoading.value && _hasMoreData.value) {
            fetchFood(_currentPage.value + 1)
        }
    }
    
    /**
     * Refresh data makanan (mulai dari halaman 1)
     */
    fun refreshFood() {
        _currentPage.value = 1
        fetchFood(1)
    }

    /**
     * Mengambil data makanan berdasarkan tanggal
     */
    fun fetchFoodDate(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                repository.getFoodByDate(date).fold(
                    onSuccess = { foodList ->
                        _foodList.value = foodList
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Terjadi kesalahan saat mengambil data"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Mengambil data makanan berdasarkan ID
     */
    fun fetchFoodById(foodId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                repository.getFoodById(foodId, forceRefresh).fold(
                    onSuccess = { foodItem ->
                        _food.value = foodItem
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Makanan tidak ditemukan"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Menganalisis gambar makanan
     */
    fun analyzeImage(imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                repository.analyzeImage(imageFile).fold(
                    onSuccess = { result ->
                        _analysisResult.value = result
                        _uploadSuccess.value = true
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Gagal menganalisis gambar"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Menganalisis gambar dengan model kustom
     */
    fun analyzeWithCustomModel(imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                repository.analyzeWithCustomModel(imageFile).fold(
                    onSuccess = { result ->
                        _customModelResult.value = result
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Gagal menganalisis gambar dengan model kustom"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Menganalisis gambar dengan TensorFlow Lite
     */
    fun analyzeWithTFLite(imagePath: String, context: android.content.Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                repository.analyzeWithTFLite(imagePath, context).fold(
                    onSuccess = { result ->
                        // Gunakan analisis result yang sama untuk TFLite
                        _analysisResult.value = result
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Gagal menganalisis gambar dengan TFLite"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Mengunggah data makanan
     */
    fun uploadFood(food: Food, imageFile: File?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            
            try {
                repository.uploadFood(food, imageFile).fold(
                    onSuccess = { result ->
                        _uploadResult.value = result
                        _successMessage.value = "Berhasil mengunggah data makanan"
                        _uploadSuccess.value = true  // Set upload success flag
                        _dataUpdated.value = true    // Signal data update
                        refreshFood()
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Gagal mengunggah data makanan"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Mengunggah data makanan dengan EditableFoodData
     * Overloaded method untuk backward compatibility dengan views
     */
//    fun uploadFood(imagePath: String?, foodData: EditableFoodData) {
//        viewModelScope.launch {
//            _isLoading.value = true
//            _errorMessage.value = null
//            _successMessage.value = null
//
//            try {
//                val imageFile = imagePath?.let { File(it) }
//
//                // Convert EditableFoodData to NutritionData
//                val nutritionData = NutritionData(
//                    calories = foodData.calories.toDoubleOrNull() ?: 0.0,
//                    carbs = foodData.carbs.toDoubleOrNull() ?: 0.0,
//                    protein = foodData.protein.toDoubleOrNull() ?: 0.0,
//                    totalFat = foodData.totalFat.toDoubleOrNull() ?: 0.0,
//                    saturatedFat = foodData.saturatedFat.toDoubleOrNull() ?: 0.0,
//                    fiber = foodData.fiber.toDoubleOrNull() ?: 0.0,
//                    sugar = foodData.sugar.toDoubleOrNull() ?: 0.0
//                )
//
//                // Create Food object from EditableFoodData
//                val food = Food(
//                    id = "",  // Will be generated by server
//                    userId = "",  // Will be assigned by server
//                    foodName = foodData.foodName,
//                    mealType = foodData.mealType ?: "dinner",
//                    weightInGrams = foodData.weightInGrams,
//                    nutritionData = nutritionData,
//                    imageUrl = null,
//                    createdAt = null
//                )
//
//                // Call our standard uploadFood method with the prepared data
//                uploadFood(food, imageFile)
//            } catch (e: Exception) {
//                _errorMessage.value = e.message ?: "Terjadi kesalahan"
//                _isLoading.value = false
//            }
//        }
//    }
//
    /**
     * Memperbarui data makanan
     */
    fun updateFood(
        foodId: String,
        imagePath: String?,
        updateData: UpdateFoodData
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            
            try {
                repository.updateFood(foodId, imagePath, updateData).fold(
                    onSuccess = { result ->
                        _editResult.value = result
                        _food.value = result
                        _successMessage.value = "Berhasil memperbarui data makanan"
                        refreshFood()
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Gagal memperbarui data makanan"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Menghapus makanan berdasarkan ID
     */
    fun deleteFood(foodId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                repository.deleteFood(foodId).fold(
                    onSuccess = {
                        _food.value = null
                        _isDeleted.value = true
                        _successMessage.value = "Berhasil menghapus makanan"
                        refreshFood()
                        onComplete()
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Gagal menghapus makanan"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Menghapus gambar makanan berdasarkan ID
     */
    fun deleteFoodImage(foodId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _imageDeletedMessage.value = null
            _errorMessage.value = null
            _successMessage.value = null
            
            try {
                repository.deleteFoodImage(foodId).fold(
                    onSuccess = { updatedFood ->
                        _imageDeletedMessage.value = "Gambar berhasil dihapus"
                        _successMessage.value = "Gambar berhasil dihapus"
                        _food.value = updatedFood  // Update the current food with the image removed
                        _isDeleted.value = false   // We're not deleting the food, just the image
                        // Refresh food detail
                        fetchFoodById(foodId, true)
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Gagal menghapus gambar"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Mengambil ringkasan harian
     */
    fun fetchDailySummary() {
        viewModelScope.launch {
            try {
                repository.getDailySummary().fold(
                    onSuccess = { summary ->
                        _dailySummary.value = summary
                    },
                    onFailure = { error ->
                        Log.e("FoodViewModel", "Failed to fetch daily summary: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("FoodViewModel", "Error fetching daily summary", e)
            }
        }
    }
    
    /**
     * Mengambil ringkasan mingguan
     */
    fun fetchWeeklySummary() {
        viewModelScope.launch {
            try {
                repository.getWeeklySummary().fold(
                    onSuccess = { summary ->
                        _weeklySummary.value = summary
                    },
                    onFailure = { error ->
                        Log.e("FoodViewModel", "Failed to fetch weekly summary: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("FoodViewModel", "Error fetching weekly summary", e)
            }
        }
    }
    
    /**
     * Mengambil preferensi pengguna
     */
//    fun fetchUserPreferences() {
//        viewModelScope.launch {
//            try {
//                repository.getUserPreferences().fold(
//                    onSuccess = { preferences ->
//                        _userPreferences.value = preferences
//                    },
//                    onFailure = { error ->
//                        Log.e("FoodViewModel", "Failed to fetch user preferences: ${error.message}")
//                    }
//                )
//            } catch (e: Exception) {
//                Log.e("FoodViewModel", "Error fetching user preferences", e)
//            }
//        }
//    }
    
    /**
     * Reset pesan error dan success
     */
    fun resetMessages() {
        _errorMessage.value = null
        _successMessage.value = null
        _imageDeletedMessage.value = null
    }
    fun resetState() {
        _uploadSuccess.value = false
        _errorMessage.value = null
        _dataUpdated.value = false
    }
    /**
     * Reset flag deleted
     */
    fun resetDeletedFlag() {
        _isDeleted.value = false
    }
}