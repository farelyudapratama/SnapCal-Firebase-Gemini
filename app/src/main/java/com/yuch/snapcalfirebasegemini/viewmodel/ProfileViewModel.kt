package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.data.api.response.ProfileRequest
import com.yuch.snapcalfirebasegemini.data.api.response.UserPreferences
import com.yuch.snapcalfirebasegemini.data.repository.ProfileRepository
import com.yuch.snapcalfirebasegemini.data.repository.ProfileNotFoundException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ApiStatus {
    object Idle : ApiStatus()
    object Loading : ApiStatus()
    data class Success(val message: String) : ApiStatus()
    data class Error(val message: String) : ApiStatus()
}

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()

    private val _fetchStatus = MutableStateFlow<ApiStatus>(ApiStatus.Idle)
    val fetchStatus: StateFlow<ApiStatus> = _fetchStatus.asStateFlow()

    private val _updateStatus = MutableStateFlow<ApiStatus>(ApiStatus.Idle)
    val updateStatus: StateFlow<ApiStatus> = _updateStatus.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchUserPreferences()
    }

    private fun fetchUserPreferences() {
        viewModelScope.launch {
            _fetchStatus.value = ApiStatus.Loading
            _isLoading.value = true
            try {
                // Panggil fungsi dari repository
                val result = repository.getProfile()
                _userPreferences.value = result
                _fetchStatus.value = ApiStatus.Success("Profile loaded")
            } catch (e: ProfileNotFoundException) { // <-- Tangkap exception kustom ini
                _userPreferences.value = null
                _fetchStatus.value = ApiStatus.Success("No profile found, ready for onboarding.")
            } catch (e: Exception) {
                _fetchStatus.value = ApiStatus.Error(e.message ?: "Failed to fetch profile")
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun saveOrUpdateProfile(profileRequest: ProfileRequest, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _updateStatus.value = ApiStatus.Loading
            _isLoading.value = true
            try {
                // Panggil fungsi dari repository
                repository.saveProfile(profileRequest)
                _updateStatus.value = ApiStatus.Success("Profile saved successfully!")
                // Setelah sukses, fetch data terbaru untuk memperbarui semua layar
                fetchUserPreferences() // Anda bisa memanggil ini atau langsung update state lokal
                onComplete(true)
            } catch (e: Exception) {
                _updateStatus.value = ApiStatus.Error(e.message ?: "Failed to save profile")
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshProfile() {
        fetchUserPreferences()
    }

    fun clearData() {
        _userPreferences.value = null
        _fetchStatus.value = ApiStatus.Idle
        _updateStatus.value = ApiStatus.Idle
        _isLoading.value = false
    }
}