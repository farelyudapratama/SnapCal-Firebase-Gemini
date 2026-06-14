package com.yuch.snapcalfirebasegemini.data.api.response

data class AiChatRequest(
    val message: String,
    val service: String,
    val userTime: String? = null,
    val timezone: String? = null
)
