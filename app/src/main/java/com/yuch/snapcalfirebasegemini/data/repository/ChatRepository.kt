package com.yuch.snapcalfirebasegemini.data.repository

import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatDelete
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatMessage
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatRequest
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatResponse
import com.yuch.snapcalfirebasegemini.data.api.response.UsageAiChat

/**
 * Repository untuk menangani operasi terkait AI Chat
 */
class ChatRepository(private val apiService: ApiService) {

    /**
     * Mendapatkan riwayat chat AI
     */
    suspend fun getAiChatHistory(): Result<List<AiChatMessage>> {
        return try {
            val response = apiService.getAiChatHistory()
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Tidak ada data riwayat chat"))
            } else {
                Result.failure(Exception("Gagal mengambil riwayat chat: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengirim pesan ke AI dan mendapatkan respon
     */
    suspend fun sendAiMessage(message: String, service: String): Result<AiChatResponse> {
        return try {
            val request = AiChatRequest(message, service)
            val response = apiService.aiMessage(request)

            if (response.isSuccessful) {
                val data = response.body()?.data
                return if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Tidak ada respon dari AI"))
                }
            } else {
                Result.failure(Exception("Gagal mengirim pesan: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mendapatkan informasi penggunaan chat AI
     */
    suspend fun getAiChatUsage(): Result<UsageAiChat> {
        return try {
            val response = apiService.getAiChatUsage()
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Tidak ada data penggunaan"))
            } else {
                Result.failure(Exception("Gagal mengambil penggunaan chat: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Menghapus seluruh riwayat chat
     */
    suspend fun deleteAiChatHistory(): Result<AiChatDelete> {
        return try {
            val response = apiService.deleteAiChatHistory()
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Tidak ada konfirmasi penghapusan"))
            } else {
                Result.failure(Exception("Gagal menghapus riwayat chat: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var instance: ChatRepository? = null

        fun getInstance(apiService: ApiService): ChatRepository =
            instance ?: synchronized(this) {
                instance ?: ChatRepository(apiService).also { instance = it }
            }
    }
}
