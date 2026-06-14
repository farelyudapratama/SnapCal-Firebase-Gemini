package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.domain.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FoodDetailViewModel(
    private val repository: ApiRepository
) : ViewModel() {
    private val _food = MutableStateFlow<FoodItem?>(null)
    val food: StateFlow<FoodItem?> = _food

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _imageDeletedMessage = MutableStateFlow<String?>(null)
    val imageDeletedMessage: StateFlow<String?> = _imageDeletedMessage

    fun fetchFoodById(foodId: String, forceRefresh: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                when (val result = repository.getFoodById(foodId, forceRefresh)) {
                    is AppResult.Success -> _food.value = result.data
                    is AppResult.Error -> _errorMessage.value = result.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFood(foodId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                when (val result = repository.deleteFood(foodId)) {
                    is AppResult.Success -> {
                        _food.value = null
                        _isDeleted.value = true
                        onComplete()
                    }
                    is AppResult.Error -> _errorMessage.value = result.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFoodImageById(foodId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                when (val result = repository.deleteFoodImage(foodId)) {
                    is AppResult.Success -> {
                        _food.value = _food.value?.copy(imageUrl = null)
                        _imageDeletedMessage.value = "Food image deleted successfully"
                    }
                    is AppResult.Error -> _errorMessage.value = result.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearImageDeletedMessage() {
        _imageDeletedMessage.value = null
    }

    fun clearIsDeleted() {
        _isDeleted.value = false
    }

    fun clearData() {
        _food.value = null
        _isLoading.value = false
        _isDeleted.value = false
        _errorMessage.value = null
        _imageDeletedMessage.value = null
    }
}
