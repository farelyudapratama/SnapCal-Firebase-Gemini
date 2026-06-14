package com.yuch.snapcalfirebasegemini.data.api.response

import com.google.gson.annotations.SerializedName

data class AiChatMessage(
    @SerializedName("_id")
    val id: String? = null,
    val role: String,
    val content: String,
    val service: String? = null,
    val timestamp: String
)

data class AiChatDelete(
    val message: String,
)
