package com.municorn.example.data.repo

import com.municorn.example.data.api.ServerStatusResponse
import com.municorn.example.data.api.StatusApi
import com.municorn.example.calls.api.dependencies.GeneralApiCall
import com.municorn.example.calls.api.entity.ApiResponse

class ServerStatusApiCall(
    private val api: StatusApi
) : GeneralApiCall<ServerStatusQuery, ServerStatusResponse> {
    override suspend fun load(query: ServerStatusQuery): ApiResponse<ServerStatusResponse> {
        return api.getStatus(query.serverName)
    }
}