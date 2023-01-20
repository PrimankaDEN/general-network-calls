package com.municorn.example.calls.api.entity

import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason.Unknown
import java.io.IOException

sealed class CallException(message: String?, cause: Throwable?) : IOException(message, cause)

/**
 * Occurs in case of local data reading or writing error
 */
class LocalCallException(
    message: String? = null,
    cause: Throwable? = null,
) : CallException(message, cause)

sealed class NetworkCallException(message: String?, cause: Throwable?) : CallException(message, cause)

/**
 * Occurs in case of regular http errors
 */
class HttpCallException(
    val httpErrorCode: Int,
    message: String? = null,
    cause: Throwable? = null,
) : NetworkCallException(message, cause)

/**
 * Occurs in case of request execution, connection or network problems.
 * For example timeouts, data parsing errors, other unexpected errors
 */
class ConnectionCallException(
    val reason: ConnectionCallExceptionReason = Unknown,
    message: String? = null,
    cause: Throwable? = null,
) : NetworkCallException(message, cause)

enum class ConnectionCallExceptionReason {
    Unknown,
    NoConnection,
    ParsingError,
    Timeout,
}
