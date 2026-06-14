package com.yuch.snapcalfirebasegemini.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.BuildConfig
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatMessage
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatRequest
import com.yuch.snapcalfirebasegemini.data.api.response.UsageResponse
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.domain.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.util.TimeZone
import java.util.UUID

class AiChatViewModel(
    private val apiRepository: ApiRepository
) : ViewModel() {
    private val _chatMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    private val _selectedService = MutableStateFlow("gemini") // Default: Gemini
    val selectedService: StateFlow<String> = _selectedService.asStateFlow()

    private val _usageInfo = MutableStateFlow<UsageResponse?>(null)
    val usageInfo: StateFlow<UsageResponse?> = _usageInfo.asStateFlow()

    private val _usageError = MutableStateFlow<String?>(null)
    val usageError: StateFlow<String?> = _usageError.asStateFlow()

    private var lastUsageFetchAt: Long = 0L

    fun setSelectedService(service: String) {
        _selectedService.value = service
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchChatHistory()
        fetchChatUsage()
    }

    private fun fetchChatHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiRepository.getAiChatHistory()
                when (response) {
                    is AppResult.Success -> {
                        _chatMessages.value = response.data
                        _errorMessage.value = null
                    }
                    is AppResult.Error -> _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun sendMessage(message: String, service: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val request = AiChatRequest(
                    message = message,
                    service = service,
                    userTime = OffsetDateTime.now().toString(),
                    timezone = TimeZone.getDefault().id
                )
                val response = apiRepository.sendAiMessage(request)
                when (response) {
                    is AppResult.Success -> {
                        fetchChatHistory()
                        fetchChatUsage(forceRefresh = true)
                    }
                    is AppResult.Error -> _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchChatUsage(forceRefresh: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!forceRefresh && _usageInfo.value != null && now - lastUsageFetchAt < USAGE_CACHE_TTL_MS) return

        viewModelScope.launch {
            try {
                val response = apiRepository.getUsage()
                when (response) {
                    is AppResult.Success -> {
                        _usageInfo.value = response.data
                        _usageError.value = null
                        lastUsageFetchAt = System.currentTimeMillis()
                    }
                    is AppResult.Error -> {
                        if (BuildConfig.DEBUG) Log.e("ChatUsage", "Error: ${response.message}")
                        _usageInfo.value = null
                        _usageError.value = response.message
                    }
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e("ChatUsage", "Exception: ${e.message}")
                _usageInfo.value = null
                _usageError.value = "Unable to load quota information"
            }
        }
    }

    fun deleteChatHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiRepository.deleteAiChatHistory()
                when (response) {
                    is AppResult.Success -> {
                        _errorMessage.value = null
                        fetchChatHistory()
                    }
                    is AppResult.Error -> _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearData() {
        _chatMessages.value = emptyList()
        _selectedService.value = "gemini"
        _usageInfo.value = null
        _usageError.value = null
        lastUsageFetchAt = 0L
        _isLoading.value = false
        _errorMessage.value = null
    }

    private companion object {
        private const val USAGE_CACHE_TTL_MS = 2 * 60 * 1000L
    }
}
