package com.yuch.snapcalfirebasegemini.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.FoodAnalysisResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * ViewModel untuk analisis gambar makanan menggunakan API
 * @property apiService Service untuk komunikasi dengan API backend
 */
class AnalyzeViewModel(
    private val apiService: ApiService = ApiConfig.getApiService()
) : ViewModel() {

    // Region: State Management
    // --------------------------

    // StateFlow untuk menyimpan hasil analisis
    private val _analysisResult = MutableStateFlow<FoodAnalysisResponse?>(null)
    val analysisResult = _analysisResult.asStateFlow()

    // StateFlow untuk status loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // StateFlow untuk pesan error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Fungsi untuk membersihkan pesan error
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    // --------------------------

    /**
     * Fungsi utama untuk menganalisis gambar
     * @param imagePath Path lokal file gambar
     * @param service Jenis service AI yang digunakan (contoh: "gemini")
     */
    fun analyzeImage(imagePath: String, service: String) {
        viewModelScope.launch {
            try {
                // Validasi file gambar
                val validationError = validateImageFile(imagePath)
                if (validationError != null) {
                    throw Exception(validationError)
                }

                _isLoading.value = true

                // Persiapan upload gambar
                val (imagePart) = prepareImageForUpload(imagePath)
                val servicePart = service.toRequestBody("text/plain".toMediaTypeOrNull())

                // Panggilan API ke backend
                val response = apiService.analyzeFood(imagePart, servicePart)

                // Handle response
                if (response.isSuccessful) {
                    _analysisResult.value = response.body()
                    _errorMessage.value = null
                } else {
                    // Handle error response dari server
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    throw Exception("Failed to analyze image: $errorBody")
                }
            } catch (e: Exception) {
                // Log dan tampilkan error
                Log.e("AnalyzeViewModel", "Error analyzing image", e)
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Validasi file gambar sebelum upload
     * @param imagePath Path file gambar
     * @return String error message atau null jika valid
     */
    private fun validateImageFile(imagePath: String): String? {
        val file = File(imagePath)

        // Cek keberadaan file
        if (!file.exists()) {
            return "File not found"
        }

        // Validasi ekstensi file
        val allowedExtensions = listOf("jpg", "jpeg", "png", "gif")
        val extension = file.extension.lowercase()
        if (extension !in allowedExtensions) {
            return "Supported formats: JPEG, PNG, GIF"
        }

        // Validasi ukuran file (maksimal 5MB)
        val maxSize = 5 * 1024 * 1024 // 5MB
        if (file.length() > maxSize) {
            return "Maximum file size is 5MB"
        }

        return null
    }

    /**
     * Mempersiapkan file gambar untuk upload ke API
     * @param imagePath Path lokal file gambar
     * @return Pair berisi MultipartBody.Part dan MIME type
     */
    private fun prepareImageForUpload(imagePath: String): Pair<MultipartBody.Part, String> {
        val file = File(imagePath)

        // Tentukan MIME type berdasarkan ekstensi file
        val mimeType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            else -> "image/*"
        }

        // Buat request body untuk file
        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())

        // Buat multipart form data
        val imagePart = MultipartBody.Part.createFormData(
            "image",  // Nama parameter sesuai API
            file.name, // Nama file
            requestFile // Request body
        )

        return Pair(imagePart, mimeType)
    }
}