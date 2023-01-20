package com.municorn.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ServerStatusResponse(
    @Json(name = "players")
    val players: Int,
    @Json(name = "server_version")
    val serverVersion: String,
)