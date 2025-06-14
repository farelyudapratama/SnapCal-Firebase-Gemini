package com.yuch.snapcalfirebasegemini.data.model

/**
 * Data class untuk menyimpan informasi pesan chat
 */
data class ChatMessage(
    val id: String,
    val role: String,
    val content: String,
    val timestamp: String
)

/**
 * Data class untuk permintaan chat AI
 */
data class ChatRequest(
    val message: String
)

/**
 * Data class untuk respons chat AI
 */
data class ChatResponse(
    val aiResponse: String
)

/**
 * Data class untuk menyimpan penggunaan AI Chat
 */
data class ChatUsage(
    val totalMessages: Int,
    val dailyLimit: Int,
    val monthlyLimit: Int,
    val resetDate: String
)
