package com.municorn.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_status")
data class StatusEntity(
    @PrimaryKey
    val server: String,
    val players: Int,
    val version: String
)
