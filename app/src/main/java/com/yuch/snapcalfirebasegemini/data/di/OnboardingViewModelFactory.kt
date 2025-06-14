package com.yuch.snapcalfirebasegemini.data.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.repository.ProfileRepository
import com.yuch.snapcalfirebasegemini.viewmodel.OnboardingViewModel

/**
 * ViewModelFactory spesifik untuk OnboardingViewModel
 */
class OnboardingViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            val repository = ProfileRepository.getInstance(apiService)
            return OnboardingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
    
    companion object {
        @Volatile
        private var instance: OnboardingViewModelFactory? = null
        
        fun getInstance(apiService: ApiService): OnboardingViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: OnboardingViewModelFactory(apiService).also { instance = it }
            }
    }
}
