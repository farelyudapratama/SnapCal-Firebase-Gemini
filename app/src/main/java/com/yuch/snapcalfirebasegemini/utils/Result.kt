package com.yuch.snapcalfirebasegemini.utils

import retrofit2.Response

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

fun handleApiError(response: Response<*>): String {
    return when (response.code()) {
        401 -> "Session expired, please login again"
        403 -> "Access denied"
        429 -> "Too many requests, try again later"
        else -> "Server error: ${response.code()}"
    }
}