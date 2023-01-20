package com.municorn.example.calls.internal

import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import com.municorn.example.calls.api.Dispatchers
import com.municorn.example.calls.api.entity.ApiResponse
import com.municorn.example.calls.api.entity.ConnectionCallException
import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason
import com.municorn.example.calls.api.entity.NetworkCallException
import com.municorn.example.calls.api.entity.RetryPolicy
import kotlinx.coroutines.withContext

/**
 * It allows retry known [NetworkCallException] and catch unexpected exceptions
 */
@AnyThread
internal suspend fun <R, T> safeNetworkCall(
    dispatchers: Dispatchers,
    retryPolicy: RetryPolicy,
    @WorkerThread
    apiCall: suspend () -> ApiResponse<R>,
    @WorkerThread
    onSuccess: suspend (R) -> T,
    @WorkerThread
    onError: suspend (NetworkCallException) -> T,
): T {
    return withContext(dispatchers.IO) {
        val response = try {
            retryOnCallExceptions(
                retryPolicy = retryPolicy,
                apiCall = { apiCall() },
            )
        } catch (e: Exception) {
            return@withContext onError(
                ConnectionCallException(
                    ConnectionCallExceptionReason.Unknown,
                    "Unexpected call exception",
                    e,
                ),
            )
        }
        when (response) {
            is ApiResponse.Success -> onSuccess(response.response)
            is ApiResponse.Error -> onError(response.error)
        }
    }
}
