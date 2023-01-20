package com.municorn.example.calls.api.dependencies

import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.System.currentTimeMillis

interface GeneralLocalStorage<Q, E> {
    @AnyThread
    fun observe(query: Q): Flow<E?>

    @WorkerThread
    @Throws(Exception::class)
    suspend fun write(query: Q, entity: E)

    @WorkerThread
    @Throws(Exception::class)
    suspend fun read(query: Q): E?
}

class MemoryLocalStorage<Q, E> : GeneralLocalStorage<Q, E> {
    private val stateFlow = MutableStateFlow<Map<Q, E>>(emptyMap())
    private val writeMutex = Mutex()

    override fun observe(query: Q): Flow<E?> {
        return stateFlow
            .map { it[query] }
            .distinctUntilChanged()
    }

    override suspend fun write(query: Q, entity: E) {
        writeMutex.withLock {
            stateFlow.emit(stateFlow.value.plus(query to entity))
        }
    }

    override suspend fun read(query: Q): E? = stateFlow.value[query]
}

class IgnoreQueryStorageWrapper<E>(private val storage: GeneralLocalStorage<Unit, E>) : GeneralLocalStorage<Any, E> {
    override fun observe(query: Any): Flow<E?> = storage.observe(Unit)
    override suspend fun write(query: Any, entity: E) = storage.write(Unit, entity)
    override suspend fun read(query: Any): E? = storage.read(Unit)
}

class TimestampStorageWrapper<Q, E>(
    private val storage: GeneralLocalStorage<Q, E>,
    private val cacheLifetimeMillis: Long = 3 * 60 * 1000,
) : GeneralLocalStorage<Q, E> {
    private val timestamp = mutableMapOf<Q, Long>()

    override fun observe(query: Q): Flow<E?> = storage.observe(query)
    override suspend fun write(query: Q, entity: E) {
        timestamp[query] = currentTimeMillis()
        storage.write(query, entity)
    }

    override suspend fun read(query: Q): E? = storage.read(query)

    fun isExpired(query: Q) = currentTimeMillis() - timestamp.getOrElse(query) { 0L } > cacheLifetimeMillis
}
