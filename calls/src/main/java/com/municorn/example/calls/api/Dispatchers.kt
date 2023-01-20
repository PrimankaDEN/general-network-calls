package com.municorn.example.calls.api

import kotlinx.coroutines.CoroutineDispatcher

@Suppress("PropertyName")
interface Dispatchers {
    val Main: CoroutineDispatcher
    val IO: CoroutineDispatcher
}

class DispatchersImpl : Dispatchers {
    override val Main get() = kotlinx.coroutines.Dispatchers.Main
    override val IO get() = kotlinx.coroutines.Dispatchers.IO
}