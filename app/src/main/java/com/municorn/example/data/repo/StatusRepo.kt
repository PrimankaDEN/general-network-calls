package com.municorn.example.data.repo

import com.municorn.example.Status
import com.municorn.example.calls.api.Dispatchers
import com.municorn.example.calls.api.call.generalCachedNetworkCall
import com.municorn.example.calls.api.call.generalLocalCall
import com.municorn.example.calls.api.entity.GeneralStorageResult
import com.municorn.example.calls.api.entity.hasError
import com.municorn.example.calls.api.entity.withDefaultPayload
import com.municorn.example.domain.StatusGateway

class StatusRepo(
    private val dispatchers: Dispatchers,
    private val apiCall: ServerStatusApiCall,
    private val storage: ServerStatusLocalStorage,
    private val mapper: ServerStatusMapper
) : StatusGateway {
    override suspend fun getStatus(server: String, force: Boolean): GeneralStorageResult<Status> {
        val query = ServerStatusQuery(server)
        return if (force) {
            generalCachedNetworkCall(query, dispatchers, apiCall, storage, mapper)
        } else {
            generalLocalCall(query, dispatchers, storage, mapper)
                .takeIf { !it.hasError }
                ?: generalCachedNetworkCall(query, dispatchers, apiCall, storage, mapper)
        }
            .withDefaultPayload(Status(0))
    }
}
