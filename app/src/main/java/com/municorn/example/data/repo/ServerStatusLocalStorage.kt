package com.municorn.example.data.repo

import com.municorn.example.calls.api.dependencies.GeneralLocalStorage
import com.municorn.example.data.database.StatusDao
import com.municorn.example.data.database.StatusEntity
import kotlinx.coroutines.flow.Flow

class ServerStatusLocalStorage(
    private val dao: StatusDao
) : GeneralLocalStorage<ServerStatusQuery, StatusEntity> {
    override fun observe(query: ServerStatusQuery): Flow<StatusEntity?> =
        dao.observeStatus(query.serverName)

    override suspend fun write(query: ServerStatusQuery, entity: StatusEntity) =
        dao.insertStatus(entity.copy(server = query.serverName))

    override suspend fun read(query: ServerStatusQuery): StatusEntity? =
        dao.getStatus(query.serverName)
}