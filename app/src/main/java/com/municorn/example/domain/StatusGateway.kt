package com.municorn.example.domain

import com.municorn.example.Status
import com.municorn.example.calls.api.entity.GeneralStorageResult

interface StatusGateway {
    suspend fun getStatus(server: String, force: Boolean = false): GeneralStorageResult<Status>
}

