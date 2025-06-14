package com.yuch.snapcalfirebasegemini.data.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yuch.snapcalfirebasegemini.data.repository.FirebaseRepository
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel

/**
 * ViewModelFactory spesifik untuk AuthViewModel
 */
class AuthViewModelFactory(
    private val repository: FirebaseRepository = FirebaseRepository.getInstance()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
