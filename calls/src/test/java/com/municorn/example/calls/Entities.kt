package com.municorn.example.calls

import com.municorn.example.calls.api.dependencies.GeneralDataMapper
import com.municorn.example.calls.api.entity.ConnectionCallException
import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason
import com.municorn.example.calls.api.entity.RetryPolicy


internal data class Query(val value: Int = 1)
internal data class Payload(val value: Int = 1)
internal data class Entity(val value: Int = 1)
internal data class Response(val value: Int = 1)

internal open class Mapper : GeneralDataMapper<Payload, Response, Entity> {
    override fun responseToPayload(response: Response): Payload {
        return Payload(response.value)
    }

    override fun responseToEntity(response: Response): Entity {
        return Entity(response.value)
    }

    override fun entityToPayload(entity: Entity): Payload {
        return Payload(entity.value)
    }
}

internal val retryTimeout = RetryPolicy(
    times = 5,
    initialDelay = 0,
    shouldRetry = { it is ConnectionCallException && it.reason == ConnectionCallExceptionReason.Timeout },
)
