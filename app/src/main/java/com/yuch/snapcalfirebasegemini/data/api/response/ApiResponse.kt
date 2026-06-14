package com.yuch.snapcalfirebasegemini.data.api.response

data class ApiResponse<T>(
    val status: String,
    val message: String? = null,
    val data: T? = null,
    val code: String? = null,
    val byokActive: Boolean? = null,
    val error: ErrorResponse? = null
)

data class ErrorResponse(
    val code: Int,
    val description: String
)
