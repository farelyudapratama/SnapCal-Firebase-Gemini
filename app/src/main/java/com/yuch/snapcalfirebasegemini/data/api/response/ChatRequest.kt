package com.yuch.snapcalfirebasegemini.data.api.response

data class AiChatRequest(
    val message: String,
    val service: String // "gemini" or "groq"
)