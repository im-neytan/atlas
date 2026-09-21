package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PedidoFila
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoFilaDao {
    @Query("SELECT * FROM pedidos_fila ORDER BY id ASC")
    fun getAllFlow(): Flow<List<PedidoFila>>

    @Query("SELECT * FROM pedidos_fila ORDER BY id ASC")
    suspend fun getAll(): List<PedidoFila>

    @Query("SELECT * FROM pedidos_fila WHERE status = 'AGUARDANDO' ORDER BY id ASC LIMIT 1")
    suspend fun getProximoAguardando(): PedidoFila?

    @Query("SELECT COUNT(*) FROM pedidos_fila")
    fun getTotalCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pedidos_fila WHERE status = 'AGUARDANDO'")
    fun getAguardandoCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pedidos_fila WHERE status = 'EM PROCESSAMENTO'")
    fun getEmProcessamentoCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pedido: PedidoFila): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pedidos: List<PedidoFila>)

    @Update
    suspend fun update(pedido: PedidoFila)

    @Delete
    suspend fun delete(pedido: PedidoFila)

    @Query("DELETE FROM pedidos_fila WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM pedidos_fila")
    suspend fun clearAll()

    @Query("DELETE FROM pedidos_fila WHERE status = 'AGUARDANDO'")
    suspend fun clearPending()
}
