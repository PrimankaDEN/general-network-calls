package com.municorn.example.calls.api.dependencies

import androidx.annotation.WorkerThread
import com.municorn.example.calls.api.entity.ApiResponse
import com.municorn.example.calls.api.entity.ConnectionCallException
import com.municorn.example.calls.api.entity.RetryPolicy

@WorkerThread
interface GeneralApiCall<Q, R> {
    /**
     * Call network request here.
     * You should manually convert http and connection errors into [ApiResponse.Error].
     * Use [RetryPolicy] to retry chosen errors
     * @throws Exception You can throw unexpected exception here, but it will never be retried.
     * Error will be converted into [ConnectionCallException] with [UnknownError] reason
     */
    @Throws(Exception::class)
    suspend fun load(query: Q): ApiResponse<R>
}
