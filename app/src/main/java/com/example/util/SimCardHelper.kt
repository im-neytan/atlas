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

import com.example.data.model.SimCard

data class RealSimCard(
    val slotIndex: Int, // 0 = SIM 1, 1 = SIM 2
    val slotDisplay: Int, // 1 ou 2
    val subscriptionId: Int,
    val carrierName: String,
    val displayName: String,
    val phoneNumber: String,
    val isDefaultVoice: Boolean
)

object SimCardHelper {

    /**
     * Tenta obter o número de telefone registrado na assinatura do cartão SIM.
     */
    fun obterNumeroTelefone(context: Context, subManager: SubscriptionManager?, info: SubscriptionInfo): String {
        var numero: String? = null

        // No Android 13+ (API 33), usa o método oficial getPhoneNumber com verificação de permissão
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && subManager != null) {
            try {
                val hasPhoneNumbers = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_NUMBERS
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPhoneNumbers) {
                    numero = subManager.getPhoneNumber(info.subscriptionId)
                }
            } catch (_: Exception) {}
        }

        // Fallback: propriedade direta da SubscriptionInfo (disponível desde API 22)
        if (numero.isNullOrBlank()) {
            try {
                @Suppress("DEPRECATION")
                numero = info.number
            } catch (_: Exception) {}
        }

        val limpo = numero?.trim()
        return if (!limpo.isNullOrBlank() && limpo != "0" && limpo != "null") {
            limpo
        } else {
            "Não gravado no chip"
        }
    }

    /**
     * Detecta os cartões SIM inseridos fisicamente ou ativos no aparelho
     * e identifica o nome real da rede, número de telefone e qual está ativo para chamadas.
     * Se o telefone não tiver nenhum cartão, retorna lista vazia (sem cartões ativos) e não inventa
     * nenhum T-Mobile ou outra operadora fictícia.
     */
    fun detectarSimsReais(context: Context): List<RealSimCard> {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val simState = telephonyManager?.simState ?: TelephonyManager.SIM_STATE_UNKNOWN

        // Se o modem reportar que não há cartão SIM inserido fisicamente
        if (simState == TelephonyManager.SIM_STATE_ABSENT) {
            return emptyList()
        }

        val hasPhoneState = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        // 1. Consulta o SubscriptionManager para enumerar todas as assinaturas ativas nos dois slots
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
                        val slotDisplay = if (info.simSlotIndex >= 0) info.simSlotIndex + 1 else 1
                        val slotIndex = if (info.simSlotIndex >= 0) info.simSlotIndex else 0

                        var carrier = info.carrierName?.toString()?.trim()?.ifBlank { null }
                            ?: info.displayName?.toString()?.trim()?.ifBlank { null }

                        // Desconsidera operadoras fictícias de emulador (Android, T-Mobile) caso o SIM não seja um cartão real pronto
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
                            slotIndex == 0
                        }

                        val phoneNum = obterNumeroTelefone(context, subManager, info)

                        RealSimCard(
                            slotIndex = slotIndex,
                            slotDisplay = slotDisplay,
                            subscriptionId = info.subscriptionId,
                            carrierName = resolvedCarrier,
                            displayName = info.displayName?.toString()?.ifBlank { null } ?: "SIM $slotDisplay",
                            phoneNumber = phoneNum,
                            isDefaultVoice = isVoice
                        )
                    }.sortedBy { it.slotDisplay }

                    return list
                }
            }
        }

        // 2. Fallback caso SubscriptionManager ou permissão READ_PHONE_STATE não esteja disponível
        val rawCarrier = telephonyManager?.networkOperatorName?.trim()?.ifBlank { null }
            ?: telephonyManager?.simOperatorName?.trim()?.ifBlank { null }

        // Se o estado não for pronto ou for operadora padrão de emulador, não inventa cartões
        if (simState != TelephonyManager.SIM_STATE_READY ||
            rawCarrier == null ||
            rawCarrier.equals("Android", ignoreCase = true) ||
            rawCarrier.equals("T-Mobile", ignoreCase = true)) {
            return emptyList()
        }

        var line1Num = "Não gravado no chip"
        try {
            val rawNum = telephonyManager?.line1Number
            if (!rawNum.isNullOrBlank()) {
                line1Num = rawNum.trim()
            }
        } catch (_: Exception) {}

        return listOf(
            RealSimCard(
                slotIndex = 0,
                slotDisplay = 1,
                subscriptionId = 1,
                carrierName = rawCarrier,
                displayName = "SIM 1",
                phoneNumber = line1Num,
                isDefaultVoice = true
            )
        )
    }

    /**
     * Resolve o estado real e detalhado dos DOIS slots de SIM do aparelho (SIM 1 e SIM 2),
     * cobrindo os cenários de 2 SIMs, 1 SIM ou nenhum SIM (0).
     */
    fun resolverEstadoSimsCompletos(
        context: Context,
        simsExistentesDb: Map<Int, SimCard> = emptyMap()
    ): List<SimCard> {
        val detected = detectarSimsReais(context)
        val now = System.currentTimeMillis()

        // Identifica SIM do slot 1 e slot 2
        val realSim1 = detected.firstOrNull { it.slotDisplay == 1 }
            ?: if (detected.size == 1 && detected[0].slotDisplay != 2) detected[0] else null

        val realSim2 = detected.firstOrNull { it.slotDisplay == 2 }
            ?: if (detected.size > 1 && detected[1] != realSim1) detected[1] else null

        // Slot 1
        val existing1 = simsExistentesDb[1]
        val sim1 = if (realSim1 != null) {
            val phone = if (realSim1.phoneNumber != "Não gravado no chip") {
                realSim1.phoneNumber
            } else {
                existing1?.phoneNumber?.takeIf { it.isNotBlank() && it != "N/A" } ?: realSim1.phoneNumber
            }
            val isVoice = realSim1.isDefaultVoice
            val remaining = existing1?.remainingSends ?: 10
            val totalLimit = existing1?.totalLimit ?: 10
            val status = when {
                remaining <= 0 -> "Cota Esgotada (0/$totalLimit)"
                isVoice -> "Ativo para Chamadas e USSD"
                else -> "Standby / Em espera"
            }
            SimCard(
                slot = 1,
                providerName = realSim1.carrierName,
                phoneNumber = phone,
                status = status,
                isInserted = true,
                isActiveVoice = isVoice,
                remainingSends = remaining,
                totalLimit = totalLimit,
                subscriptionId = realSim1.subscriptionId,
                displayName = realSim1.displayName,
                lastUpdated = now
            )
        } else {
            SimCard(
                slot = 1,
                providerName = "Sem cartões ativos",
                phoneNumber = "N/A",
                status = if (detected.isEmpty()) "Sem cartões ativos" else "Slot 1 Vazio / Desocupado",
                isInserted = false,
                isActiveVoice = false,
                remainingSends = 0,
                totalLimit = 10,
                subscriptionId = -1,
                displayName = "SIM 1",
                lastUpdated = now
            )
        }

        // Slot 2
        val existing2 = simsExistentesDb[2]
        val sim2 = if (realSim2 != null) {
            val phone = if (realSim2.phoneNumber != "Não gravado no chip") {
                realSim2.phoneNumber
            } else {
                existing2?.phoneNumber?.takeIf { it.isNotBlank() && it != "N/A" } ?: realSim2.phoneNumber
            }
            val isVoice = realSim2.isDefaultVoice
            val remaining = existing2?.remainingSends ?: 10
            val totalLimit = existing2?.totalLimit ?: 10
            val status = when {
                remaining <= 0 -> "Cota Esgotada (0/$totalLimit)"
                isVoice -> "Ativo para Chamadas e USSD"
                else -> "Standby / Em espera"
            }
            SimCard(
                slot = 2,
                providerName = realSim2.carrierName,
                phoneNumber = phone,
                status = status,
                isInserted = true,
                isActiveVoice = isVoice,
                remainingSends = remaining,
                totalLimit = totalLimit,
                subscriptionId = realSim2.subscriptionId,
                displayName = realSim2.displayName,
                lastUpdated = now
            )
        } else {
            SimCard(
                slot = 2,
                providerName = "Sem cartões ativos",
                phoneNumber = "N/A",
                status = if (detected.isEmpty()) "Sem cartões ativos" else "Slot 2 Vazio / Desocupado",
                isInserted = false,
                isActiveVoice = false,
                remainingSends = 0,
                totalLimit = 10,
                subscriptionId = -1,
                displayName = "SIM 2",
                lastUpdated = now
            )
        }

        return listOf(sim1, sim2)
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
