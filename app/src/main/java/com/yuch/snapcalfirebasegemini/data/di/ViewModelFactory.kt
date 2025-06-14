package com.yuch.snapcalfirebasegemini.data.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.repository.ChatRepository
import com.yuch.snapcalfirebasegemini.data.repository.ProfileRepository
import com.yuch.snapcalfirebasegemini.viewmodel.AiChatViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel

/**
 * ViewModelFactory spesifik untuk ProfileViewModel
 */
class ProfileViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val repository = ProfileRepository.getInstance(apiService)
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
    
    companion object {
        @Volatile
        private var instance: ProfileViewModelFactory? = null
        
        fun getInstance(apiService: ApiService): ProfileViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ProfileViewModelFactory(apiService).also { instance = it }
            }
    }
}

/**
 * ViewModelFactory spesifik untuk AiChatViewModel
 */
class AiChatViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiChatViewModel::class.java)) {
            val repository = ChatRepository.getInstance(apiService)
            return AiChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
    
    companion object {
        @Volatile
        private var instance: AiChatViewModelFactory? = null
        
        fun getInstance(apiService: ApiService): AiChatViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: AiChatViewModelFactory(apiService).also { instance = it }
            }
    }
}
