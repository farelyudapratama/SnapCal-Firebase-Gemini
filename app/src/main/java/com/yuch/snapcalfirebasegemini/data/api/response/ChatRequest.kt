package com.yuch.snapcalfirebasegemini.data.api.response

data class ChatRequest(
    val message: String,
    val service: String // "gemini" or "groq"
)