package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SimCard
import kotlinx.coroutines.flow.Flow

/**
 * DAO para armazenamento e gerenciamento dos dados e estado real
 * dos cartões SIM instalados no telefone.
 */
@Dao
interface SimCardDao {

    @Query("SELECT * FROM sim_cards ORDER BY slot ASC")
    fun getAllFlow(): Flow<List<SimCard>>

    @Query("SELECT * FROM sim_cards ORDER BY slot ASC")
    suspend fun getAll(): List<SimCard>

    @Query("SELECT * FROM sim_cards WHERE slot = :slot LIMIT 1")
    fun getBySlotFlow(slot: Int): Flow<SimCard?>

    @Query("SELECT * FROM sim_cards WHERE slot = :slot LIMIT 1")
    suspend fun getBySlot(slot: Int): SimCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(simCard: SimCard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(simCards: List<SimCard>)

    @Update
    suspend fun update(simCard: SimCard)

    @Query("UPDATE sim_cards SET status = :status WHERE slot = :slot")
    suspend fun updateStatus(slot: Int, status: String)

    @Query("UPDATE sim_cards SET remainingSends = :remaining WHERE slot = :slot")
    suspend fun updateRemainingSends(slot: Int, remaining: Int)

    @Query("UPDATE sim_cards SET phoneNumber = :phoneNumber WHERE slot = :slot")
    suspend fun updatePhoneNumber(slot: Int, phoneNumber: String)

    @Query("UPDATE sim_cards SET isActiveVoice = (slot == :activeSlot)")
    suspend fun setActiveVoiceSlot(activeSlot: Int)

    @Query("DELETE FROM sim_cards WHERE slot = :slot")
    suspend fun deleteBySlot(slot: Int)

    @Query("DELETE FROM sim_cards")
    suspend fun clearAll()
}
