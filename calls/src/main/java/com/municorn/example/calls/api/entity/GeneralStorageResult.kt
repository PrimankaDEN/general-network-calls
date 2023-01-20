package com.municorn.example.calls.api.entity

import java.util.Date

sealed class GeneralStorageResult<P> {
    internal abstract val timestamp: Date

    /**
     * You can use it to show local data while remote request is executing
     */
    data class Loading<P>(
        val payload: P? = null,
        override val timestamp: Date = Date(),
    ) : GeneralStorageResult<P>()

    data class Success<P>(
        val payload: P,
        override val timestamp: Date = Date(),
    ) : GeneralStorageResult<P>()

    data class Error<P>(
        val payload: P?,
        val errors: List<CallException>,
        override val timestamp: Date = Date(),
    ) : GeneralStorageResult<P>() {
        companion object {
            operator fun <P> invoke(
                payload: P?,
                vararg errors: CallException,
                timestamp: Date = Date(),
            ) = Error(payload, errors.asList(), timestamp)

            operator fun <P> invoke(
                vararg errors: CallException,
                timestamp: Date = Date(),
            ) = Error<P>(null, errors.asList(), timestamp)
        }
    }
}
