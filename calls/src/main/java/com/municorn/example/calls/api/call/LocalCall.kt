package com.municorn.example.calls.api.call

import androidx.annotation.AnyThread
import com.municorn.example.calls.api.Dispatchers
import com.municorn.example.calls.api.dependencies.GeneralLocalDataMapper
import com.municorn.example.calls.api.dependencies.GeneralLocalStorage
import com.municorn.example.calls.api.entity.GeneralStorageResult
import com.municorn.example.calls.api.entity.GeneralStorageResult.Error
import com.municorn.example.calls.api.entity.GeneralStorageResult.Success
import com.municorn.example.calls.api.entity.LocalCallException
import kotlinx.coroutines.withContext

/**
 * It allows safely read local data from DB or shared prefs
 * Catches reading and parsing errors
 * Use it when you want read local data without triggering network request
 */
@AnyThread
suspend fun <Q, P, E> generalLocalCall(
    query: Q,
    dispatchers: Dispatchers,
    storage: GeneralLocalStorage<Q, E>,
    mapper: GeneralLocalDataMapper<P, E>,
): GeneralStorageResult<P> {
    return withContext(dispatchers.IO) {
        val entity = try {
            storage.read(query)
        } catch (caughtException: Exception) {
            return@withContext Error(null, LocalCallException("Failed local data reading", caughtException))
        } ?: return@withContext Error(null, LocalCallException("No local data"))
        return@withContext try {
            Success(mapper.entityToPayload(entity))
        } catch (caughtException: Exception) {
            Error(null, LocalCallException("Failed entity to payload mapping", caughtException))
        }
    }
}

@AnyThread
suspend fun <Q, P> generalLocalCall(
    query: Q,
    dispatchers: Dispatchers,
    storage: GeneralLocalStorage<Q, P>,
): GeneralStorageResult<P> {
    return generalLocalCall(
        query,
        dispatchers,
        storage,
        object : GeneralLocalDataMapper<P, P> {
            override fun entityToPayload(entity: P): P = entity
        },
    )
}
