package com.yuch.snapcalfirebasegemini.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatMessage
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatRequest
import com.yuch.snapcalfirebasegemini.data.api.response.UsageAiChat
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AiChatViewModel(
    private val apiRepository: ApiRepository
) : ViewModel() {
    private val _chatMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    private val _selectedService = MutableStateFlow("gemini") // Default: Gemini
    val selectedService: StateFlow<String> = _selectedService.asStateFlow()

    private val _usageInfo = MutableStateFlow<UsageAiChat?>(null)
    val usageInfo: StateFlow<UsageAiChat?> = _usageInfo.asStateFlow()

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
                if (response.isSuccessful) {
                    _chatMessages.value = response.body()?.data ?: emptyList()
                } else {
                    _errorMessage.value = "Failed to load chat history"
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
            try {
                val request = AiChatRequest(message, service)
                val response = apiRepository.sendAiMessage(request)
                if (response.isSuccessful) {
                    fetchChatHistory()
                } else {
                    _errorMessage.value = "Failed to send message"
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchChatUsage() {
        viewModelScope.launch {
            try {
                val response = apiRepository.getAiChatUsage()
                if (response.isSuccessful) {
                    _usageInfo.value = response.body()?.data
                } else {
                    Log.e("ChatUsage", "Error: ${response.errorBody()?.string()}")
                    // Handle error
                    _usageInfo.value = null
                    _errorMessage.value = "Failed to load chat usage"
                }
            } catch (e: Exception) {
                Log.e("ChatUsage", "Exception: ${e.message}")
                // Handle exception
                _usageInfo.value = null
                _errorMessage.value = "An error occurred: ${e.message}"
            }
        }
    }

    fun deleteChatHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiRepository.deleteAiChatHistory()
                if (response.isSuccessful) {
                    fetchChatHistory()
                } else {
                    _errorMessage.value = "Failed to delete chat history"
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
        _isLoading.value = false
        _errorMessage.value = null
    }
}
