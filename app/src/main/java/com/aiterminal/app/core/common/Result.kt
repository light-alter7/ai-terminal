package com.aiterminal.app.core.common

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : AppResult<Nothing>()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw cause ?: RuntimeException(message)
    }

    inline fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}
