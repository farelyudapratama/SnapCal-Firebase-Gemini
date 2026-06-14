package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.data.api.request.toUpdateFoodParts
import com.yuch.snapcalfirebasegemini.data.api.request.toUploadFoodParts
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import com.yuch.snapcalfirebasegemini.data.model.UpdateFoodData
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.domain.result.AppResult
import com.yuch.snapcalfirebasegemini.utils.normalizeDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodEntryViewModel(
    private val repository: ApiRepository
) : ViewModel() {

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

    fun uploadFood(imagePath: String?, foodData: EditableFoodData) {
        if (foodData.mealType == null) {
            _errorMessage.value = "Please select a meal type"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

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
                val foodParts = safeFoodData.toUploadFoodParts(imagePath)

                when (val result = repository.uploadFood(
                    image = foodParts.image,
                    foodName = foodParts.foodName,
                    mealType = foodParts.mealType,
                    weightInGrams = foodParts.weightInGrams,
                    nutritionData = foodParts.nutritionData
                )) {
                    is AppResult.Success -> _uploadSuccess.value = true
                    is AppResult.Error -> _errorMessage.value = result.toDisplayMessage()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFood(foodId: String, imagePath: String?, foodData: UpdateFoodData?) {
        if (foodData?.mealType == null) {
            _errorMessage.value = "Please select a meal type"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val foodParts = foodData.toUpdateFoodParts(imagePath)

                when (val result = repository.updateFood(
                    id = foodId,
                    foodName = foodParts.foodName,
                    mealType = foodParts.mealType,
                    weightInGrams = foodParts.weightInGrams,
                    nutritionData = foodParts.nutritionData,
                    image = foodParts.image
                )) {
                    is AppResult.Success -> _successMessage.value = "Food updated successfully."
                    is AppResult.Error -> _errorMessage.value = result.toDisplayMessage()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _uploadSuccess.value = false
        _errorMessage.value = null
    }

    fun clearData() {
        _isLoading.value = false
        _errorMessage.value = null
        _successMessage.value = null
        _uploadSuccess.value = false
    }

    private fun AppResult.Error.toDisplayMessage(): String =
        code?.let { "[Code: $it] $message" } ?: message
}
