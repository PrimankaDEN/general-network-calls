package com.municorn.example.calls

import com.municorn.example.calls.api.call.generalCachedNetworkCall
import com.municorn.example.calls.api.dependencies.GeneralApiCall
import com.municorn.example.calls.api.dependencies.GeneralLocalStorage
import com.municorn.example.calls.api.entity.ApiResponse
import com.municorn.example.calls.api.entity.ConnectionCallException
import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason.NoConnection
import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason.ParsingError
import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason.Timeout
import com.municorn.example.calls.api.entity.GeneralStorageResult
import com.municorn.example.calls.api.entity.LocalCallException
import com.municorn.example.calls.api.entity.errorOrNull
import com.municorn.example.calls.api.entity.payloadOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
internal class GeneralCachedNetworkCallTest {
    private val mapper = spy<Mapper>()
    private val api = mock<GeneralApiCall<Query, Response>>()
    private val db = mock<GeneralLocalStorage<Query, Entity>>()

    @Test
    fun `should return error on first failed loading`() = runTest {
        whenever(db.read(Query())).thenReturn(null)
        whenever(api.load(Query())).thenReturn(
            ApiResponse.Error(
                ConnectionCallException(
                    NoConnection
                )
            )
        )

        val result =
            generalCachedNetworkCall(Query(), TestDispatchers(), api, db, mapper, retryTimeout)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertNull(result.payloadOrNull)
        assertEquals(NoConnection, (result.errorOrNull as ConnectionCallException).reason)
    }

    @Test
    fun `should return saved data on first failed loading`() = runTest {
        whenever(db.read(Query())).thenReturn(Entity())
        whenever(api.load(Query())).thenReturn(
            ApiResponse.Error(
                ConnectionCallException(
                    NoConnection
                )
            )
        )

        val result =
            generalCachedNetworkCall(Query(), TestDispatchers(), api, db, mapper, retryTimeout)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertEquals(Payload(), result.payloadOrNull)
        assertEquals(NoConnection, (result.errorOrNull as ConnectionCallException).reason)
    }

    @Test
    fun `should retry exception`() = runTest {
        whenever(db.read(Query())).thenReturn(null)
        whenever(api.load(Query())).thenReturn(ApiResponse.Error(ConnectionCallException(Timeout)))

        val result = generalCachedNetworkCall(
            Query(),
            TestDispatchers(testScheduler),
            api,
            db,
            mapper,
            retryTimeout
        )

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertNull(result.payloadOrNull)
        assertEquals(Timeout, (result.errorOrNull as ConnectionCallException).reason)
        verify(api, times(5)).load(Query())
    }

    @Test
    fun `should save loaded data`() = runTest {
        whenever(db.read(Query())).thenReturn(null)
        whenever(api.load(Query())).thenReturn(ApiResponse.Success(Response()))

        val result =
            generalCachedNetworkCall(Query(), TestDispatchers(), api, db, mapper, retryTimeout)

        assertIs<GeneralStorageResult.Success<Payload>>(result)
        assertEquals(Payload(), result.payloadOrNull)
        verify(db, times(1)).write(Query(), Entity())
    }

    @Test
    fun `should return error on data reading exception`() = runTest {
        whenever(db.read(Query())).thenThrow(NullPointerException())
        whenever(api.load(Query())).thenReturn(
            ApiResponse.Error(
                ConnectionCallException(
                    NoConnection
                )
            )
        )

        val result =
            generalCachedNetworkCall(Query(), TestDispatchers(), api, db, mapper, retryTimeout)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertNull(result.payloadOrNull)
        assertEquals(
            NoConnection,
            (result.errors.first { it is ConnectionCallException } as ConnectionCallException).reason,
        )
        assertIs<NullPointerException>(result.errors.first { it is LocalCallException }.cause)
    }

    @Test
    fun `should return error on data saving exception`() = runTest {
        whenever(db.read(Query())).thenReturn(null)
        whenever(db.write(Query(), Entity())).thenThrow(NullPointerException())
        whenever(api.load(Query())).thenReturn(ApiResponse.Success(Response()))

        val result =
            generalCachedNetworkCall(Query(), TestDispatchers(), api, db, mapper, retryTimeout)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertEquals(Payload(), result.payloadOrNull)
        assertIs<LocalCallException>(result.errorOrNull)
        assertIs<NullPointerException>(result.errorOrNull?.cause)
    }

    @Test
    fun `should return error on response to entity mapping exception`() = runTest {
        whenever(db.read(Query())).thenReturn(null)
        whenever(api.load(Query())).thenReturn(ApiResponse.Success(Response()))
        whenever(mapper.responseToEntity(Response())).thenThrow(NullPointerException())

        val result =
            generalCachedNetworkCall(Query(), TestDispatchers(), api, db, mapper, retryTimeout)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertEquals(Payload(), result.payloadOrNull)
        assertIs<LocalCallException>(result.errorOrNull)
    }

    @Test
    fun `should return error on response to payload mapping exception`() = runTest {
        whenever(db.read(Query())).thenReturn(null)
        whenever(api.load(Query())).thenReturn(ApiResponse.Success(Response()))
        whenever(mapper.responseToPayload(Response())).thenThrow(NullPointerException())

        val result =
            generalCachedNetworkCall(Query(), TestDispatchers(), api, db, mapper, retryTimeout)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertNull(result.payloadOrNull)
        assertEquals(ParsingError, (result.errorOrNull as ConnectionCallException).reason)
    }

    @Test
    fun `should return error on entity to payload mapping exception`() = runTest {
        whenever(db.read(Query())).thenReturn(Entity())
        whenever(api.load(Query())).thenThrow(NullPointerException())
        whenever(mapper.entityToPayload(Entity())).thenThrow(NumberFormatException())

        val result =
            generalCachedNetworkCall(Query(), TestDispatchers(), api, db, mapper, retryTimeout)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertNull(result.payloadOrNull)
        assertIs<NullPointerException>(result.errors.first { it is ConnectionCallException }.cause)
        assertIs<NumberFormatException>(result.errors.first { it is LocalCallException }.cause)
    }
}
