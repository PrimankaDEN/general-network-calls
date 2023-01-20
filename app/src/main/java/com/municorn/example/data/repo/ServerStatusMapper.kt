package com.municorn.example.data.repo

import com.municorn.example.Status
import com.municorn.example.calls.api.dependencies.GeneralDataMapper
import com.municorn.example.data.api.ServerStatusResponse
import com.municorn.example.data.database.StatusEntity

class ServerStatusMapper : GeneralDataMapper<Status, ServerStatusResponse, StatusEntity> {
    override fun entityToPayload(entity: StatusEntity): Status = Status(entity.players)

    override fun responseToEntity(response: ServerStatusResponse): StatusEntity =
        StatusEntity("", response.players, response.serverVersion)
}