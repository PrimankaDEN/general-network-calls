package com.municorn.example.calls

import com.municorn.example.calls.api.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@ExperimentalCoroutinesApi
class TestDispatchers(private val scheduler: TestCoroutineScheduler? = null) : Dispatchers {
    override val Main get() = UnconfinedTestDispatcher(scheduler)
    override val IO get() = UnconfinedTestDispatcher(scheduler)
}