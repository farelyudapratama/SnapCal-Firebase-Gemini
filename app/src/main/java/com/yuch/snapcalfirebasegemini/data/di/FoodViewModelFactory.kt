package com.yuch.snapcalfirebasegemini.data.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.local.FoodDao
import com.yuch.snapcalfirebasegemini.data.repository.FoodRepository
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel

/**
 * Factory untuk membuat instance FoodViewModel
 * Menggabungkan fungsionalitas dari FoodViewModel dan GetFoodViewModel
 */
class FoodViewModelFactory(
    private val apiService: ApiService,
    private val foodDao: FoodDao?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            val repository = FoodRepository.getInstance(apiService, foodDao)
            return FoodViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
    
    companion object {
        @Volatile
        private var instance: FoodViewModelFactory? = null
        
        fun getInstance(apiService: ApiService, foodDao: FoodDao?): FoodViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: FoodViewModelFactory(apiService, foodDao).also { instance = it }
            }
    }
}
