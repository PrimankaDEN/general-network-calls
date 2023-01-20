package com.municorn.example.calls

import com.municorn.example.calls.api.call.generalNetworkCall
import com.municorn.example.calls.api.dependencies.GeneralApiCall
import com.municorn.example.calls.api.entity.ApiResponse
import com.municorn.example.calls.api.entity.ConnectionCallException
import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason.NoConnection
import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason.ParsingError
import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason.Timeout
import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason.Unknown
import com.municorn.example.calls.api.entity.GeneralStorageResult
import com.municorn.example.calls.api.entity.errorOrNull
import com.municorn.example.calls.api.entity.payloadOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertIs
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
internal class GeneralNetworkCallTest {
    private val mapper = spy<Mapper>()
    private val api = mock<GeneralApiCall<Query, Response>>()

    @Test
    fun `should return error on failed loading`() = runTest {
        whenever(api.load(Query())).thenReturn(
            ApiResponse.Error(
                ConnectionCallException(
                    NoConnection
                )
            )
        )

        val result = generalNetworkCall(Query(), TestDispatchers(), api, mapper, retryTimeout)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertNull(result.payloadOrNull)
        assertEquals(NoConnection, (result.errorOrNull as ConnectionCallException).reason)
        verify(api, times(1)).load(Query())
    }

    @Test
    fun `should return error on unexpected loading error`() = runTest {
        whenever(api.load(Query())).thenThrow(NullPointerException())

        val result = generalNetworkCall(Query(), TestDispatchers(), api, mapper, retryTimeout)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertNull(result.payloadOrNull)
        assertEquals(Unknown, (result.errorOrNull as ConnectionCallException).reason)
        verify(api, times(1)).load(Query())
    }

    @Test
    fun `should return error on failed mapping`() = runTest {
        whenever(api.load(Query())).thenReturn(ApiResponse.Success(Response()))
        whenever(mapper.responseToPayload(Response())).thenThrow(NullPointerException())

        val result = generalNetworkCall(Query(), TestDispatchers(), api, mapper, retryTimeout)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertNull(result.payloadOrNull)
        assertEquals(ParsingError, (result.errorOrNull as ConnectionCallException).reason)
        assertIs<NullPointerException>(result.errorOrNull?.cause)
        verify(api, times(1)).load(Query())
    }

    @Test
    fun `should retry exception`() = runTest {
        whenever(api.load(Query())).thenReturn(ApiResponse.Error(ConnectionCallException(Timeout)))

        val result =
            generalNetworkCall(Query(), TestDispatchers(testScheduler), api, mapper, retryTimeout)

        assertIs<GeneralStorageResult.Error<Payload>>(result)
        assertNull(result.payloadOrNull)
        assertEquals(Timeout, (result.errorOrNull as ConnectionCallException).reason)
        verify(api, times(5)).load(Query())
    }

    @Test
    fun `should return data`() = runTest {
        whenever(api.load(Query())).thenReturn(ApiResponse.Success(Response()))

        val result = generalNetworkCall(Query(), TestDispatchers(), api, mapper, retryTimeout)

        assertIs<GeneralStorageResult.Success<Payload>>(result)
        assertEquals(Payload(), result.payloadOrNull)
        verify(api, times(1)).load(Query())
    }
}
