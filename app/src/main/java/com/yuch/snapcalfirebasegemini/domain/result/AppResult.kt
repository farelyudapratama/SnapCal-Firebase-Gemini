package com.yuch.snapcalfirebasegemini.domain.result

sealed interface AppResult<out T> {
    data class Success<T>(
        val data: T,
        val message: String? = null
    ) : AppResult<T>

    data class Error(
        val message: String,
        val code: Int? = null,
        val errorCode: String? = null,
        val cause: Throwable? = null
    ) : AppResult<Nothing>
}
