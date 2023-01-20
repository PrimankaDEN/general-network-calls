package com.municorn.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 1,
    exportSchema = false,
    entities = [
        StatusEntity::class,
    ],
)
abstract class StatusDatabase : RoomDatabase() {
    abstract fun statusDao(): StatusDao
}