package com.yuch.snapcalfirebasegemini.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.mapper.toFoodItem
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.domain.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FoodListViewModel(
    private val repository: ApiRepository
) : ViewModel() {
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

    // State loading untuk pagination (load more)
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

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
            Log.d("FoodListViewModel", "Fetching food data for page: $page")

            // Gunakan isLoading untuk initial load dan isLoadingMore untuk pagination
            if (page == 1) {
                _isLoading.value = true
            } else {
                _isLoadingMore.value = true
            }

            _errorMessage.value = null
            try {
                // Coba ambil data dari API
                val result = repository.getAllFood(page)
                if (result is AppResult.Error) {
                    throw Exception(result.message)
                }

                val foodPage = (result as AppResult.Success).data
                val fetchedItems = foodPage.items

                // Jika halaman pertama, replace data; jika tidak, append data
                _foodList.value = if (page == 1) {
                    fetchedItems
                } else {
                    _foodList.value + fetchedItems
                }
                _currentPage.value = page
                _totalPages.value = foodPage.totalPages
                _hasMoreData.value = _currentPage.value < _totalPages.value
            } catch (e: Exception) {
                // Jika terjadi error (misalnya tidak ada internet), ambil data dari cache
                _errorMessage.value = e.message
                val cachedData = repository.getCachedFoods()
                _foodList.value = cachedData.map { it.toFoodItem() }
                // Set flag hasMoreData false jika data cache digunakan (atau Anda bisa logika lain)
                _hasMoreData.value = false
            } finally {
                if (page == 1) {
                    _isLoading.value = false
                } else {
                    _isLoadingMore.value = false
                }
            }
        }
    }

    fun fetchFoodDate(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                when (val result = repository.getFoodDate(date)) {
                    is AppResult.Success -> _foodList.value = result.data
                    is AppResult.Error -> _errorMessage.value = result.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
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

    fun clearData() {
        _foodList.value = emptyList()
        _currentPage.value = 1
        _totalPages.value = 1
        _hasMoreData.value = true
        _isLoading.value = false
        _isLoadingMore.value = false
        _errorMessage.value = null
    }
}
