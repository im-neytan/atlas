package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Room que armazena e gerencia os detalhes de cada cartão SIM
 * detectado no dispositivo (SIM 1 e SIM 2).
 */
@Entity(tableName = "sim_cards")
data class SimCard(
    @PrimaryKey
    val slot: Int, // 1 ou 2
    val providerName: String, // Nome do provedor/operadora (ex: Vodacom, Movitel, Tmcel)
    val phoneNumber: String, // Número de telefone registrado ou formatado
    val status: String, // Estado real (ex: "Ativo para Chamadas", "Standby / Em espera", "Slot Vazio / Ausente")
    val isInserted: Boolean = false, // Se há chip físico inserido no slot
    val isActiveVoice: Boolean = false, // Se é a linha padrão para chamadas e comandos USSD
    val remainingSends: Int = 10,
    val totalLimit: Int = 10,
    val subscriptionId: Int = -1,
    val displayName: String = "SIM $slot",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isLimitReached: Boolean
        get() = isInserted && remainingSends <= 0

    val progressRatio: Float
        get() = if (totalLimit > 0) remainingSends.toFloat() / totalLimit.toFloat() else 0f
}
