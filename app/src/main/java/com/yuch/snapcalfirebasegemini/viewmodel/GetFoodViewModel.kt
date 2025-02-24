package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.FoodPage
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionData
import com.yuch.snapcalfirebasegemini.data.local.FoodEntity
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.utils.formatDateFromLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GetFoodViewModel(private val repository: ApiRepository) : ViewModel() {
    private val _foodList = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodList: StateFlow<List<FoodItem>> = _foodList

    private val _food = MutableStateFlow<FoodItem?>(null)
    val food: StateFlow<FoodItem?> = _food

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

    init {
        // Ambil data dari halaman pertama
        fetchFood(page = 1)
    }

    /**
     * Mengambil data makanan dari API berdasarkan halaman.
     * Jika halaman = 1, data akan direplace (refresh), sedangkan halaman > 1 akan diappend.
     */
    private fun fetchFood(page: Int = _currentPage.value) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Coba ambil data dari API
                val response: ApiResponse<FoodPage>? = repository.getAllFood(page)
                val fetchedItems = response?.data?.items ?: emptyList()

                // Jika halaman pertama, replace data; jika tidak, append data
                _foodList.value = if (page == 1) {
                    fetchedItems
                } else {
                    _foodList.value + fetchedItems
                }
                _currentPage.value = page
                if (response != null) {
                    _totalPages.value = response.data?.totalPages ?: 1
                }
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
                        createdAt = formatDateFromLong(it.createdAt)
                    )
                }
                // Set flag hasMoreData false jika data cache digunakan (atau Anda bisa logika lain)
                _hasMoreData.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchFoodById(foodId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val foodData = repository.getFoodById(foodId)
                if (foodData != null) {
                    _food.value = foodData
                } else {
                    _errorMessage.value = "Makanan tidak ditemukan"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
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
}