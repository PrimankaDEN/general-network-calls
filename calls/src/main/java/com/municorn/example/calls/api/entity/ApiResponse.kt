package com.municorn.example.calls.api.entity

sealed class ApiResponse<T> {
    data class Success<T>(val response: T) : ApiResponse<T>()
    data class Error<T>(val error: NetworkCallException) : ApiResponse<T>()
}

fun <T, R> ApiResponse<T>.map(map: T.() -> R): ApiResponse<R> {
    return when (this) {
        is ApiResponse.Success -> ApiResponse.Success(response.map())
        is ApiResponse.Error -> ApiResponse.Error(error)
    }
}
