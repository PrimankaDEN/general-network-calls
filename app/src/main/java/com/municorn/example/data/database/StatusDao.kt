package com.municorn.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusDao {
    @Query("SELECT * FROM server_status WHERE server IS :server")
    suspend fun getStatus(server: String): StatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(server: StatusEntity)

    @Query("SELECT * FROM server_status WHERE server IS :server")
    fun observeStatus(server: String): Flow<StatusEntity>
}