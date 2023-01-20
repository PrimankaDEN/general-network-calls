@file:Suppress("BlockingMethodInNonBlockingContext")

package com.municorn.example.calls.api.call

import androidx.annotation.AnyThread
import com.municorn.example.calls.api.Dispatchers
import com.municorn.example.calls.api.dependencies.GeneralApiCall
import com.municorn.example.calls.api.dependencies.GeneralNetworkDataMapper
import com.municorn.example.calls.api.entity.ApiResponse
import com.municorn.example.calls.api.entity.ConnectionCallException
import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason
import com.municorn.example.calls.api.entity.GeneralStorageResult
import com.municorn.example.calls.api.entity.RetryPolicy
import com.municorn.example.calls.internal.safeNetworkCall

/**
 * Use it when you need just call remote request without saving it locally or caching
 * To retry some network errors see [RetryPolicy]
 */
@AnyThread
suspend fun <Q, P, R> generalNetworkCall(
    query: Q,
    dispatchers: Dispatchers,
    apiCall: GeneralApiCall<Q, R>,
    mapper: GeneralNetworkDataMapper<P, R>,
    retryPolicy: RetryPolicy = RetryPolicy(),
): GeneralStorageResult<P> {
    return safeNetworkCall(
        dispatchers,
        retryPolicy,
        { apiCall.load(query) },
        { response ->
            try {
                val payload = mapper.responseToPayload(response)
                GeneralStorageResult.Success(payload)
            } catch (caughtException: Exception) {
                GeneralStorageResult.Error(
                    ConnectionCallException(
                        ConnectionCallExceptionReason.ParsingError,
                        "Failed response to payload mapping",
                        caughtException,
                    ),
                )
            }
        },
        { error ->
            GeneralStorageResult.Error(null, error)
        },
    )
}

@AnyThread
suspend fun <Q, P> generalNetworkCall(
    query: Q,
    dispatchers: Dispatchers,
    apiCall: GeneralApiCall<Q, P>,
    retryPolicy: RetryPolicy = RetryPolicy(),
): GeneralStorageResult<P> {
    return generalNetworkCall(
        query,
        dispatchers,
        apiCall,
        object : GeneralNetworkDataMapper<P, P> {
            override fun responseToPayload(response: P): P = response
        },
        retryPolicy,
    )
}

@AnyThread
suspend fun <Q, P> generalNetworkCall(
    query: Q,
    dispatchers: Dispatchers,
    retryPolicy: RetryPolicy = RetryPolicy(),
    apiCall: suspend (query: Q) -> ApiResponse<P>,
): GeneralStorageResult<P> {
    return generalNetworkCall(
        query,
        dispatchers,
        object : GeneralApiCall<Q, P> {
            override suspend fun load(query: Q): ApiResponse<P> = apiCall(query)
        },
        retryPolicy,
    )
}
