package com.example.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
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
     * Resolve o estado dos cartões SIM REAIS presentes no aparelho (0, 1 ou 2 SIMs).
     * Retorna EXCLUSIVAMENTE os cartões físicos/eSIM reais que estão inseridos e ativos,
     * sem criar slots vazios fictícios como se fossem cartões presentes.
     */
    fun resolverSimsReaisPresentes(
        context: Context,
        simsExistentesDb: Map<Int, SimCard> = emptyMap()
    ): List<SimCard> {
        val detected = detectarSimsReais(context)
        val now = System.currentTimeMillis()

        return detected.map { real ->
            val existing = simsExistentesDb[real.slotDisplay]
            val phone = if (real.phoneNumber != "Não gravado no chip") {
                real.phoneNumber
            } else {
                existing?.phoneNumber?.takeIf { it.isNotBlank() && it != "N/A" } ?: real.phoneNumber
            }
            val remaining = existing?.remainingSends ?: 10
            val totalLimit = existing?.totalLimit ?: 10
            val status = when {
                remaining <= 0 -> "Cota Esgotada (0/$totalLimit)"
                real.isDefaultVoice -> "SIM Padrão para Chamadas"
                else -> "Secundário / Standby"
            }

            SimCard(
                slot = real.slotDisplay,
                providerName = real.carrierName,
                phoneNumber = phone,
                status = status,
                isInserted = true,
                isActiveVoice = real.isDefaultVoice,
                remainingSends = remaining,
                totalLimit = totalLimit,
                subscriptionId = real.subscriptionId,
                displayName = "SIM ${real.slotDisplay}",
                lastUpdated = now
            )
        }
    }

    /**
     * Resolve o estado dos slots (mantido para compatibilidade com partes existentes do sistema)
     */
    fun resolverEstadoSimsCompletos(
        context: Context,
        simsExistentesDb: Map<Int, SimCard> = emptyMap()
    ): List<SimCard> {
        return resolverSimsReaisPresentes(context, simsExistentesDb)
    }

    /**
     * Aplica a troca real do SIM padrão de chamadas no telefone (Android OS)
     * SEM abrir telas de configurações ou definições externas.
     * Utiliza APIs de TelecomManager, SubscriptionManager e configurações de telefonia do sistema.
     */
    fun aplicarTrocaSimPadraoChamadasSistema(context: Context, targetSlot: Int, targetSubId: Int): Boolean {
        var sucesso = false

        // 1. TelecomManager: Define a conta telefônica de saída selecionada pelo usuário
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            val targetHandle = obterPhoneAccountHandleParaSim(context, targetSlot, targetSubId)
            if (telecomManager != null && targetHandle != null) {
                try {
                    val method = telecomManager.javaClass.getMethod(
                        "setUserSelectedOutgoingPhoneAccount",
                        PhoneAccountHandle::class.java
                    )
                    method.isAccessible = true
                    method.invoke(telecomManager, targetHandle)
                    sucesso = true
                    Log.d("SimCardHelper", "TelecomManager: setUserSelectedOutgoingPhoneAccount aplicado para $targetHandle")
                } catch (e: Exception) {
                    Log.w("SimCardHelper", "TelecomManager setUserSelectedOutgoingPhoneAccount não acessível: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.w("SimCardHelper", "Erro ao acessar TelecomManager: ${e.message}")
        }

        // 2. SubscriptionManager: Define o SubId de voz padrão no sistema via reflexão
        if (targetSubId > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                if (subManager != null) {
                    try {
                        val methodVoice = subManager.javaClass.getMethod("setDefaultVoiceSubId", Int::class.javaPrimitiveType)
                        methodVoice.isAccessible = true
                        methodVoice.invoke(subManager, targetSubId)
                        sucesso = true
                        Log.d("SimCardHelper", "SubscriptionManager: setDefaultVoiceSubId($targetSubId) aplicado")
                    } catch (_: Exception) {
                        try {
                            val methodVoiceStatic = SubscriptionManager::class.java.getMethod("setDefaultVoiceSubId", Int::class.javaPrimitiveType)
                            methodVoiceStatic.isAccessible = true
                            methodVoiceStatic.invoke(null, targetSubId)
                            sucesso = true
                        } catch (_: Exception) {}
                    }

                    // Sincroniza também dados e SMS para consistência operacional
                    try {
                        val methodData = subManager.javaClass.getMethod("setDefaultDataSubId", Int::class.javaPrimitiveType)
                        methodData.isAccessible = true
                        methodData.invoke(subManager, targetSubId)
                    } catch (_: Exception) {}

                    try {
                        val methodSms = subManager.javaClass.getMethod("setDefaultSmsSubId", Int::class.javaPrimitiveType)
                        methodSms.isAccessible = true
                        methodSms.invoke(subManager, targetSubId)
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                Log.w("SimCardHelper", "SubscriptionManager alteração falhou: ${e.message}")
            }
        }

        // 3. Provedor de Configurações Globais / do Sistema de Telefonia
        if (targetSubId > 0) {
            try {
                Settings.Global.putInt(context.contentResolver, "multi_sim_voice_call", targetSubId)
                sucesso = true
            } catch (_: Exception) {}

            try {
                Settings.System.putInt(context.contentResolver, "multi_sim_voice_call", targetSubId)
                sucesso = true
            } catch (_: Exception) {}

            try {
                Settings.Global.putInt(context.contentResolver, "voice_call_sim_setting", targetSubId)
                sucesso = true
            } catch (_: Exception) {}

            try {
                Settings.Global.putInt(context.contentResolver, "multi_sim_voice_prompt", 0)
            } catch (_: Exception) {}
        }

        return sucesso
    }

    /**
     * Localiza o PhoneAccountHandle correspondente ao SIM slot ou SubscriptionId
     * para que chamadas e comandos USSD sejam disparados diretamente pelo chip especificado.
     */
    fun obterPhoneAccountHandleParaSim(context: Context, simSlot: Int, subscriptionId: Int): PhoneAccountHandle? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null

        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager ?: return null
            val accounts: List<PhoneAccountHandle> = try {
                telecomManager.callCapablePhoneAccounts ?: emptyList()
            } catch (_: SecurityException) {
                emptyList()
            }

            if (accounts.isEmpty()) return null

            // 1. Tenta correspondência direta pelo Subscription ID contido no ID da PhoneAccount
            if (subscriptionId > 0) {
                val matchBySub = accounts.firstOrNull { handle ->
                    val id = handle.id ?: ""
                    id == subscriptionId.toString() || id.contains(subscriptionId.toString())
                }
                if (matchBySub != null) return matchBySub
            }

            // 2. Tenta por índice de slot (0 para SIM 1, 1 para SIM 2)
            val slotIndex = (simSlot - 1).coerceAtLeast(0)
            if (slotIndex in accounts.indices) {
                return accounts[slotIndex]
            }

            return accounts.firstOrNull()
        } catch (e: Exception) {
            Log.w("SimCardHelper", "Erro ao obter PhoneAccountHandle: ${e.message}")
            return null
        }
    }

    /**
     * Abre a tela oficial do sistema Android para alternância do SIM padrão de chamadas
     * através de TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS ou configurações de rede/SIM.
     */
    fun abrirConfiguracoesAlternarSimSistema(context: Context): Boolean {
        val telecomAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS
        } else {
            "android.telephony.action.CHANGE_PHONE_ACCOUNTS"
        }

        val intents = listOf(
            Intent(telecomAction),
            Intent("android.telephony.action.CHANGE_PHONE_ACCOUNTS"),
            Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
            Intent("android.settings.MANAGE_ALL_SIM_PROFILES_SETTINGS"),
            Intent("android.settings.SIM_MANAGEMENT_SETTINGS"),
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )

        for (intent in intents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return true
            } catch (_: Exception) {
                // Tenta a próxima intenção suportada pelo fabricante
            }
        }
        return false
    }

    /**
     * Abre a tela de gerenciamento de Cartões SIM e Chamadas do sistema Android
     * para que o usuário confirme a troca do chip padrão para chamadas se desejado.
     */
    fun abrirConfiguracoesSim(context: Context) {
        abrirConfiguracoesAlternarSimSistema(context)
    }
}
