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

/**
 * Status API untuk operasi profil
 */
sealed class ApiStatus {
    object Idle : ApiStatus()
    object Loading : ApiStatus()
    data class Success(val message: String) : ApiStatus()
    data class Error(val message: String) : ApiStatus()
}

/**
 * ViewModel untuk menangani operasi terkait profil pengguna
 */
class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()

    private val _fetchStatus = MutableStateFlow<ApiStatus>(ApiStatus.Idle)
    val fetchStatus: StateFlow<ApiStatus> = _fetchStatus.asStateFlow()

    private val _updateStatus = MutableStateFlow<ApiStatus>(ApiStatus.Idle)
    val updateStatus: StateFlow<ApiStatus> = _updateStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchUserPreferences()
    }

    /**
     * Mengambil preferensi pengguna dari repository
     */
    fun fetchUserPreferences() {
        viewModelScope.launch {
            _isLoading.value = true
            _fetchStatus.value = ApiStatus.Loading
            try {
                // Panggil fungsi dari repository
                val result = repository.getProfile()
                _userPreferences.value = result
                _fetchStatus.value = ApiStatus.Success("Profile loaded")
            } catch (e: ProfileNotFoundException) { 
                _userPreferences.value = null
                _fetchStatus.value = ApiStatus.Success("No profile found, ready for onboarding.")
            } catch (e: Exception) {
                _fetchStatus.value = ApiStatus.Error(e.message ?: "Failed to fetch profile")
            } finally {
                _isLoading.value = false
            }
        }
    }


    /**
     * Menyimpan atau memperbarui profil pengguna
     *
     * @param profileRequest Data profil yang akan disimpan
     * @param onComplete Callback yang dipanggil setelah operasi selesai
     */
    fun saveOrUpdateProfile(profileRequest: ProfileRequest, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _updateStatus.value = ApiStatus.Loading
            try {
                // Panggil fungsi dari repository
                val result = repository.saveProfile(profileRequest)
                _userPreferences.value = result // Update state secara langsung
                _updateStatus.value = ApiStatus.Success("Profile saved successfully!")
                onComplete(true)
            } catch (e: Exception) {
                _updateStatus.value = ApiStatus.Error(e.message ?: "Failed to save profile")
                onComplete(false)
            }
        }
    }

    /**
     * Reset status API ke idle
     */
    fun resetStatus() {
        _fetchStatus.value = ApiStatus.Idle
        _updateStatus.value = ApiStatus.Idle
    }
}