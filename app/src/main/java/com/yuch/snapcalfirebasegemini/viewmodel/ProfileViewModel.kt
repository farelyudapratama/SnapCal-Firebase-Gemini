package com.yuch.snapcalfirebasegemini.viewmodel

import android.util.Log
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

// Tambahkan tipe data untuk menyimpan field error
data class ProfileFieldError(
    val fieldName: String,
    val errorMessage: String
)

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()

    private val _fetchStatus = MutableStateFlow<ApiStatus>(ApiStatus.Idle)
    val fetchStatus: StateFlow<ApiStatus> = _fetchStatus.asStateFlow()

    private val _updateStatus = MutableStateFlow<ApiStatus>(ApiStatus.Idle)
    val updateStatus: StateFlow<ApiStatus> = _updateStatus.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Tambahkan StateFlow untuk field errors
    private val _fieldErrors = MutableStateFlow<List<ProfileFieldError>>(emptyList())
    val fieldErrors: StateFlow<List<ProfileFieldError>> = _fieldErrors.asStateFlow()

    init {
        fetchUserPreferences()
    }

    fun fetchUserPreferences() {
        viewModelScope.launch {
            _fetchStatus.value = ApiStatus.Loading
            _isLoading.value = true

            try {
                Log.d("ProfileViewModel", "Fetching user preferences...")
                val result = repository.getProfile()
                _userPreferences.value = result
                _fetchStatus.value = ApiStatus.Success("Profile loaded successfully")
                Log.d("ProfileViewModel", "Profile loaded: ${result.personalInfo != null}")

            } catch (e: ProfileNotFoundException) {
                Log.w("ProfileViewModel", "Profile not found: ${e.message}")
                _userPreferences.value = null
                _fetchStatus.value = ApiStatus.Success("No profile found, ready for onboarding.")

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching profile: ${e.message}")
                _userPreferences.value = null
                _fetchStatus.value = ApiStatus.Error(e.message ?: "Failed to load profile")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveOrUpdateProfile(profileRequest: ProfileRequest, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _updateStatus.value = ApiStatus.Loading
            _isLoading.value = true

            // Reset field errors setiap kali submit
            _fieldErrors.value = emptyList()

            try {
                // Validasi data sebelum dikirim ke server
                val validationErrors = validateProfileRequest(profileRequest)
                if (validationErrors.isNotEmpty()) {
                    _fieldErrors.value = validationErrors
                    _updateStatus.value = ApiStatus.Error("Please fix the errors in your profile")
                    onComplete(false)
                    return@launch
                }

                Log.d("ProfileViewModel", "Saving/updating user profile...")
                val result = repository.updateProfile(profileRequest)
                _userPreferences.value = result
                _updateStatus.value = ApiStatus.Success("Profile saved successfully")
                Log.d("ProfileViewModel", "Profile saved successfully")
                onComplete(true)

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error saving profile: ${e.message}")

                // Check if it's an authorization error
                if (e.message?.contains("unauthorized", ignoreCase = true) == true ||
                    e.message?.contains("401", ignoreCase = true) == true) {
                    _updateStatus.value = ApiStatus.Error("Authorization failed. Please login again.")
                } else {
                    _updateStatus.value = ApiStatus.Error(e.message ?: "Failed to save profile")
                }
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi untuk validasi data profile
    private fun validateProfileRequest(profileRequest: ProfileRequest): List<ProfileFieldError> {
        val errors = mutableListOf<ProfileFieldError>()

        // Validasi personal info
        profileRequest.personalInfo?.let { info ->
            if (info.age == null || info.age <= 0) {
                errors.add(ProfileFieldError("age", "Age must be a positive number"))
            }
            if (info.height == null || info.height <= 0) {
                errors.add(ProfileFieldError("height", "Height must be a positive number"))
            }
            if (info.weight == null || info.weight <= 0) {
                errors.add(ProfileFieldError("weight", "Weight must be a positive number"))
            }
            if (info.gender.isNullOrBlank()) {
                errors.add(ProfileFieldError("gender", "Gender must be selected"))
            }
            if (info.activityLevel.isNullOrBlank()) {
                errors.add(ProfileFieldError("activityLevel", "Activity level must be selected"))
            }
        } ?: errors.add(ProfileFieldError("personalInfo", "Personal information is required"))

        // Validasi daily goals
        profileRequest.dailyGoals?.let { goals ->
            if (goals.calories == null || goals.calories <= 0) {
                errors.add(ProfileFieldError("calories", "Calories must be a positive number"))
            }
        }

        return errors
    }

    // Fungsi untuk membersihkan error field tertentu
    fun clearFieldError(fieldName: String) {
        _fieldErrors.value = _fieldErrors.value.filter { it.fieldName != fieldName }
    }

    fun clearData() {
        _userPreferences.value = null
        _fetchStatus.value = ApiStatus.Idle
        _updateStatus.value = ApiStatus.Idle
        _isLoading.value = false
    }

    fun refreshProfile() {
        fetchUserPreferences()
    }
}