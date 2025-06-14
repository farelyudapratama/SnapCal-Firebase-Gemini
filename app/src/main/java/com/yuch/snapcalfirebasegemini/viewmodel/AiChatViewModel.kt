package com.yuch.snapcalfirebasegemini.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatMessage
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatRequest
import com.yuch.snapcalfirebasegemini.data.api.response.UsageAiChat
import com.yuch.snapcalfirebasegemini.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiChatViewModel(
    private val repository: ChatRepository
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
                val response = repository.getAiChatHistory()
                if (response.isSuccess) {
                    _chatMessages.value = response.getOrNull() ?: emptyList()
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Gagal mengambil riwayat chat: ${response.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun sendMessage(message: String, service: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.sendAiMessage(message, service)
                if (response.isSuccess) {
                    val newMessage = response.getOrNull()
                    if (newMessage != null) {
                        fetchChatHistory()
                    } else {
                        _errorMessage.value = "Gagal mengirim pesan"
                    }
                } else {
                    _errorMessage.value = "Gagal mendapatkan response dari AI"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchChatUsage() {
        viewModelScope.launch {
            try {
                val response = repository.getAiChatUsage()
                if (response.isSuccess) {
                    _usageInfo.value = response.getOrNull()
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Gagal mengambil penggunaan chat: ${response.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                Log.e("ChatUsage", "Exception: ${e.message}")
                // Handle exception
                _usageInfo.value = null
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
            }
        }
    }

    fun deleteChatHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.deleteAiChatHistory()
                if (response.isSuccess) {
                    _chatMessages.value = emptyList() // Clear chat messages
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Gagal menghapus riwayat chat: ${response.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

