package com.yuch.snapcalfirebasegemini.data.api.response

import com.google.gson.annotations.SerializedName

data class AiChatResponse(
    val aiResponse: String
)

data class AiChatMessage(
    @SerializedName("_id")
    val id: String,
    val role: String,
    val content: String,
    val timestamp: String
)

data class AiChatDelete(
    val message: String,
)

data class UsageAiChat(
    val gemini: UsageAiChatDetails,
    val groq: UsageAiChatDetails
)

data class UsageAiChatDetails(
    val dailyCount: Int,
    val lastUsed: String,
    val remainingQuota: Int
)
