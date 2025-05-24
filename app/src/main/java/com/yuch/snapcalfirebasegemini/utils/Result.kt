package com.yuch.snapcalfirebasegemini.utils

import retrofit2.Response

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

sealed class UiState<T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

fun handleApiError(response: Response<*>): String {
    return when (response.code()) {
        401 -> "Session expired, please login again"
        403 -> "Access denied"
        429 -> "Too many requests, try again later"
        else -> "Server error: ${response.code()}"
    }
}