package com.municorn.example.calls.internal

import com.municorn.example.calls.api.entity.ApiResponse
import com.municorn.example.calls.api.entity.NetworkCallException
import com.municorn.example.calls.api.entity.RetryPolicy
import kotlinx.coroutines.delay

/**
 * It retries only known [NetworkCallException]
 */
internal suspend fun <T> retryOnCallExceptions(
    retryPolicy: RetryPolicy,
    apiCall: suspend () -> ApiResponse<T>,
): ApiResponse<T> {
    var currentDelay = retryPolicy.initialDelay
    repeat(retryPolicy.times - 1) {
        when (val response = apiCall()) {
            is ApiResponse.Success -> return response
            is ApiResponse.Error -> if (!retryPolicy.shouldRetry(response.error)) return response
        }
        delay(currentDelay)
        currentDelay = (currentDelay * retryPolicy.factor).toLong().coerceAtMost(retryPolicy.maxDelay)
    }
    return apiCall() // last attempt
}
