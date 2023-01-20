package com.municorn.example.calls.api.entity

val <P> GeneralStorageResult<P>.payloadOrNull: P?
    get() = when (this) {
        is GeneralStorageResult.Loading -> payload
        is GeneralStorageResult.Success -> payload
        is GeneralStorageResult.Error -> payload
    }

val <P>GeneralStorageResult<P>.payloadOrThrow: P
    get() = when (this) {
        is GeneralStorageResult.Loading -> payload ?: throw IllegalStateException()
        is GeneralStorageResult.Success -> payload
        is GeneralStorageResult.Error -> payload ?: errors.firstOrNull()?.let { throw Exception(it) }
            ?: throw IllegalStateException()
    }

val <P>GeneralStorageResult<P>.hasError: Boolean get() = errorOrNull != null

val <P>GeneralStorageResult<P>.errorOrNull: CallException?
    get() = (this as? GeneralStorageResult.Error)?.errors?.firstOrNull()

val <P>GeneralStorageResult<P>.error: CallException
    get() = errorOrNull ?: throw IllegalStateException("No known errors")

val <P>GeneralStorageResult<P>.httpErrorCode: Int?
    get() = (errorOrNull as? HttpCallException)?.httpErrorCode

fun <P> GeneralStorageResult<P>.withDefaultPayload(defaultPayload: P): GeneralStorageResult<P> {
    return when (this) {
        is GeneralStorageResult.Loading -> if (payload == null) copy(payload = defaultPayload) else copy()
        is GeneralStorageResult.Success -> copy()
        is GeneralStorageResult.Error -> if (payload == null) copy(payload = defaultPayload) else copy()
    }
}

fun <P, T> GeneralStorageResult<P>.map(mapper: (P) -> T): GeneralStorageResult<T> {
    return when (this) {
        is GeneralStorageResult.Loading -> GeneralStorageResult.Loading(payload?.let { mapper(it) }, timestamp)
        is GeneralStorageResult.Success -> GeneralStorageResult.Success(mapper(payload), timestamp)
        is GeneralStorageResult.Error -> GeneralStorageResult.Error(payload?.let { mapper(it) }, errors, timestamp)
    }
}
