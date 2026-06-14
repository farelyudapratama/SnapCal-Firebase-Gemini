package com.yuch.snapcalfirebasegemini.data.api.response

data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T? = null,
    val error: ErrorResponse? = null
)

data class ErrorResponse(
    val code: Int,
    val description: String
)
