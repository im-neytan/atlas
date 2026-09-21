package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.HistoricoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoricoDao {
    @Query("SELECT * FROM historico_transferencias ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<HistoricoItem>>

    @Query("SELECT * FROM historico_transferencias ORDER BY timestamp DESC")
    suspend fun getAll(): List<HistoricoItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HistoricoItem): Long

    @androidx.room.Update
    suspend fun update(item: HistoricoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HistoricoItem>)

    @Query("DELETE FROM historico_transferencias")
    suspend fun clearAll()
}
