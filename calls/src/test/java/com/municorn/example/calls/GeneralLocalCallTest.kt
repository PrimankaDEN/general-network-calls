package com.municorn.example.calls

import com.municorn.example.calls.api.call.generalLocalCall
import com.municorn.example.calls.api.dependencies.GeneralLocalStorage
import com.municorn.example.calls.api.entity.GeneralStorageResult
import com.municorn.example.calls.api.entity.LocalCallException
import com.municorn.example.calls.api.entity.payloadOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import kotlin.test.assertIs
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class GeneralLocalCallTest {
    private val mapper = spy<Mapper>()
    private val db = mock<GeneralLocalStorage<Query, Entity>>()

    @Test
    fun `should return saved data`() = runTest {
        whenever(db.read(Query())).thenReturn(Entity())

        val result = generalLocalCall(Query(), TestDispatchers(), db, mapper)

        assertIs<GeneralStorageResult.Success<Payload>>(result)
        Assert.assertEquals(Payload(), result.payloadOrNull)
    }

    @Test
    fun `should return empty data`() = runTest {
        whenever(db.read(Query())).thenReturn(null)

        val result = generalLocalCall(Query(), TestDispatchers(), db, mapper)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertNull(result.payloadOrNull)
    }

    @Test
    fun `should return error on entity to payload mapping exception`() = runTest {
        whenever(db.read(Query())).thenReturn(Entity())
        whenever(mapper.entityToPayload(Entity())).thenThrow(NullPointerException())

        val result = generalLocalCall(Query(), TestDispatchers(), db, mapper)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertNull(result.payloadOrNull)
        assertIs<NullPointerException>(result.errors.first { it is LocalCallException }.cause)
    }

    @Test
    fun `should return error on data reading exception`() = runTest {
        whenever(db.read(Query())).thenThrow(NullPointerException())

        val result = generalLocalCall(Query(), TestDispatchers(), db, mapper)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertNull(result.payloadOrNull)
        assertIs<NullPointerException>(result.errors.first { it is LocalCallException }.cause)
    }
}
