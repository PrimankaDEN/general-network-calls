package com.municorn.example.calls.api.call

import androidx.annotation.AnyThread
import com.municorn.example.calls.api.Dispatchers
import com.municorn.example.calls.api.dependencies.GeneralApiCall
import com.municorn.example.calls.api.dependencies.GeneralDataMapper
import com.municorn.example.calls.api.dependencies.GeneralLocalStorage
import com.municorn.example.calls.api.entity.ApiResponse
import com.municorn.example.calls.api.entity.ConnectionCallException
import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason.ParsingError
import com.municorn.example.calls.api.entity.GeneralStorageResult
import com.municorn.example.calls.api.entity.GeneralStorageResult.Error
import com.municorn.example.calls.api.entity.GeneralStorageResult.Success
import com.municorn.example.calls.api.entity.LocalCallException
import com.municorn.example.calls.api.entity.RetryPolicy
import com.municorn.example.calls.internal.safeNetworkCall

/**
 * It saves remote data to local storage in case of successful network request.
 * And it returns local data in case of failed network request.
 *
 * Processes errors and exceptions on each step and tried to return any available data.
 * To retry some errors see [RetryPolicy]
 */
@AnyThread
suspend fun <Q, P, R, E> generalCachedNetworkCall(
    query: Q,
    dispatchers: Dispatchers,
    apiCall: GeneralApiCall<in Q, R>,
    storage: GeneralLocalStorage<in Q, E>,
    mapper: GeneralDataMapper<P, R, E>,
    retryPolicy: RetryPolicy = RetryPolicy(),
): GeneralStorageResult<P> {
    return safeNetworkCall(
        dispatchers,
        retryPolicy,
        { apiCall.load(query) },
        { response ->
            val payload = try {
                mapper.responseToPayload(response)
            } catch (caughtException: Exception) {
                return@safeNetworkCall Error(
                    ConnectionCallException(
                        ParsingError,
                        "Failed response to payload mapping",
                        caughtException,
                    ),
                )
            }
            val entity = try {
                mapper.responseToEntity(response)
            } catch (caughtException: Exception) {
                return@safeNetworkCall Error(
                    payload,
                    LocalCallException(
                        "Failed response to entity mapping",
                        caughtException,
                    ),
                )
            }
            return@safeNetworkCall try {
                storage.write(query, entity)
                Success(payload)
            } catch (caughtException: Exception) {
                Error(payload, LocalCallException("Failed local data writing", caughtException))
            }
        },
        { error ->
            val entity = try {
                storage.read(query)
            } catch (caughtException: Exception) {
                return@safeNetworkCall Error(
                    error,
                    LocalCallException(
                        "Failed local data reading",
                        caughtException,
                    ),
                )
            }
            return@safeNetworkCall try {
                val payload = entity?.let { mapper.entityToPayload(it) }
                Error(payload, error)
            } catch (caughtException: Exception) {
                Error(
                    error,
                    LocalCallException("Failed entity to payload mapping", caughtException)
                )
            }
        },
    )
}

@AnyThread
suspend fun <Q, P, R, E> generalCachedNetworkCall(
    query: Q,
    dispatchers: Dispatchers,
    storage: GeneralLocalStorage<Q, E>,
    mapper: GeneralDataMapper<P, R, E>,
    retryPolicy: RetryPolicy = RetryPolicy(),
    apiCall: suspend (query: Q) -> ApiResponse<R>,
): GeneralStorageResult<P> {
    return generalCachedNetworkCall(
        query,
        dispatchers,
        object : GeneralApiCall<Q, R> {
            override suspend fun load(query: Q): ApiResponse<R> = apiCall(query)
        },
        storage,
        mapper,
        retryPolicy,
    )
}
