package com.municorn.example.data.api

import com.municorn.example.calls.api.entity.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StatusApi {
    /**
     * https://esi.evetech.net/ui/?version=latest#/Status/get_status
     */
    @GET(value = "status")
    suspend fun getStatus(@Query("datasource") serverName: String?): ApiResponse<ServerStatusResponse>
}

