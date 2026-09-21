package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedidos_fila")
data class PedidoFila(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val displayId: String, // ex: "#BLK-1024"
    val numeroDestino: String, // ex: "+244 923 456 789"
    val megas: String, // ex: "500 MB", "1 GB"
    val status: String, // "AGUARDANDO", "EM PROCESSAMENTO", "FALHA", "CONCLUIDO"
    val timestamp: Long = System.currentTimeMillis(),
    val simSlot: Int = 1
)
