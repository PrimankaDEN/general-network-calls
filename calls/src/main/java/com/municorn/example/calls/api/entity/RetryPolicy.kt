package com.municorn.example.calls.api.entity

import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason.NoConnection
import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason.Timeout
import java.net.HttpURLConnection.HTTP_BAD_GATEWAY
import java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT
import java.net.HttpURLConnection.HTTP_UNAVAILABLE

data class RetryPolicy(
    val times: Int = 5,
    val initialDelay: Long = 100,
    val maxDelay: Long = 1000,
    val factor: Double = 2.0,
    val shouldRetry: (e: NetworkCallException) -> Boolean = defaultRetryRule,
)

val defaultRetryRule: (e: NetworkCallException) -> Boolean = { e ->
    when (e) {
        is HttpCallException -> e.httpErrorCode in listOf(
            HTTP_BAD_GATEWAY,
            HTTP_UNAVAILABLE,
            HTTP_GATEWAY_TIMEOUT,
        )
        is ConnectionCallException -> e.reason in listOf(
            Timeout,
            NoConnection,
        )
    }
}
