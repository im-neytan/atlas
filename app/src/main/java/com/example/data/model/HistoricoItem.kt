package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historico_transferencias")
data class HistoricoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val displayId: String,
    val numeroDestino: String,
    val megas: String,
    val status: String, // "CONCLUIDO", "FALHA", "CANCELADO"
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String,
    val ussdResposta: String,
    val simSlot: Int = 1
)
