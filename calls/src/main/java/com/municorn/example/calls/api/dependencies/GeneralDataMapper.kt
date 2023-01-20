package com.municorn.example.calls.api.dependencies

import androidx.annotation.AnyThread

@AnyThread
interface GeneralNetworkDataMapper<P, R> {
    @Throws(Exception::class)
    fun responseToPayload(response: R): P
}

@AnyThread
interface GeneralLocalDataMapper<P, E> {
    @Throws(Exception::class)
    fun entityToPayload(entity: E): P
}

@AnyThread
interface GeneralDataMapper<P, R, E> : GeneralNetworkDataMapper<P, R>, GeneralLocalDataMapper<P, E> {
    @Throws(Exception::class)
    override fun responseToPayload(response: R): P = entityToPayload(responseToEntity(response))

    @Throws(Exception::class)
    override fun entityToPayload(entity: E): P

    @Throws(Exception::class)
    fun responseToEntity(response: R): E
}
