package com.example.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

data class RealSimCard(
    val slotIndex: Int, // 0 = SIM 1, 1 = SIM 2
    val slotDisplay: Int, // 1 ou 2
    val subscriptionId: Int,
    val carrierName: String,
    val displayName: String,
    val isDefaultVoice: Boolean
)

object SimCardHelper {

    /**
     * Detecta os cartões SIM inseridos fisicamente ou ativos no aparelho
     * e identifica o nome real da rede e qual está ativo para chamadas.
     */
    fun detectarSimsReais(context: Context): List<RealSimCard> {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val simState = telephonyManager?.simState ?: TelephonyManager.SIM_STATE_UNKNOWN

        // Se o modem reportar que não há cartão SIM inserido
        if (simState == TelephonyManager.SIM_STATE_ABSENT) {
            return emptyList()
        }

        val hasPhoneState = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        // 1. Se possuir permissão READ_PHONE_STATE, consulta a lista real de assinaturas do SubscriptionManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            if (subManager != null && hasPhoneState) {
                val activeList: List<SubscriptionInfo>? = try {
                    subManager.activeSubscriptionInfoList
                } catch (_: SecurityException) {
                    null
                }

                if (activeList != null) {
                    if (activeList.isEmpty()) {
                        // O Android confirma categoricamente: 0 cartões SIM ativos
                        return emptyList()
                    }

                    val defaultVoiceSubId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        SubscriptionManager.getDefaultVoiceSubscriptionId()
                    } else {
                        SubscriptionManager.getDefaultSubscriptionId()
                    }

                    val list = activeList.mapNotNull { info ->
                        val slotDisplay = (info.simSlotIndex + 1).coerceAtLeast(1)
                        var carrier = info.carrierName?.toString()?.trim()?.ifBlank { null }
                            ?: info.displayName?.toString()?.trim()?.ifBlank { null }

                        // Desconsidera nomes fictícios de rádio de emulador
                        if (carrier != null && (carrier.equals("Android", ignoreCase = true) || carrier.equals("T-Mobile", ignoreCase = true))) {
                            if (simState != TelephonyManager.SIM_STATE_READY) {
                                return@mapNotNull null
                            }
                        }

                        val resolvedCarrier = carrier ?: "SIM $slotDisplay"
                        val isVoice = if (activeList.size == 1) {
                            true
                        } else if (defaultVoiceSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                            info.subscriptionId == defaultVoiceSubId
                        } else {
                            info.simSlotIndex == 0
                        }

                        RealSimCard(
                            slotIndex = info.simSlotIndex,
                            slotDisplay = slotDisplay,
                            subscriptionId = info.subscriptionId,
                            carrierName = resolvedCarrier,
                            displayName = info.displayName?.toString()?.ifBlank { null } ?: "SIM $slotDisplay",
                            isDefaultVoice = isVoice
                        )
                    }.sortedBy { it.slotDisplay }

                    return list
                }
            }
        }

        // 2. Sem permissão ou sem SubscriptionManager: verificar TelephonyManager
        val rawCarrier = telephonyManager?.networkOperatorName?.trim()?.ifBlank { null }
            ?: telephonyManager?.simOperatorName?.trim()?.ifBlank { null }

        // Se não há chip pronto ou se for a rede padrão fictícia de emulador (T-Mobile / Android)
        if (simState != TelephonyManager.SIM_STATE_READY ||
            rawCarrier == null ||
            rawCarrier.equals("Android", ignoreCase = true) ||
            rawCarrier.equals("T-Mobile", ignoreCase = true)) {
            // Nenhum cartão real ativo
            return emptyList()
        }

        return listOf(
            RealSimCard(
                slotIndex = 0,
                slotDisplay = 1,
                subscriptionId = 1,
                carrierName = rawCarrier,
                displayName = "SIM 1",
                isDefaultVoice = true
            )
        )
    }

    /**
     * Abre a tela de gerenciamento de Cartões SIM e Chamadas do sistema Android
     * para que o usuário confirme a troca do chip padrão para chamadas se desejado.
     */
    fun abrirConfiguracoesSim(context: Context) {
        val intents = listOf(
            Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
            Intent("android.settings.WIRELESS_SETTINGS"),
            Intent("android.settings.NETWORK_OPERATOR_SETTINGS"),
            Intent(Settings.ACTION_SETTINGS)
        )

        for (intent in intents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                // Tenta próximo
            }
        }
    }
}
