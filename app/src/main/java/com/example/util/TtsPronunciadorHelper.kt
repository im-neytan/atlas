package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Gerenciador de Pronúncias por Voz do Bl4ck System.
 * - Elimina loops e repetições infinitas através de verificação de timestamp e chave de evento única.
 * - Configura voz de alta qualidade (selecionando vozes premium/não-offline de TTS se disponíveis).
 * - Modula tom (pitch) e velocidade (speech rate) para dicção natural e clara em Português.
 * - Formata números de telefone espaçadamente para pronúncia compreensível.
 */
class TtsPronunciadorHelper private constructor(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    // Cache de deduplicação para impedir que uma mesma frase seja dita em loop
    private val narracaoDebounceMap = ConcurrentHashMap<String, Long>()
    private val NARRACAO_DEBOUNCE_MS = 6000L

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsEngine = tts ?: return
            
            // Tenta selecionar locale de Português (MZ ou BR)
            val ptLocale = Locale("pt", "MZ")
            val result = ttsEngine.setLanguage(ptLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsEngine.setLanguage(Locale("pt", "BR"))
            }

            // Otimização de voz para som de alta fidelidade
            try {
                val availableVoices = ttsEngine.voices
                if (!availableVoices.isNullOrEmpty()) {
                    // Prioriza vozes em português com maior qualidade e menor latência
                    val bestVoice = availableVoices
                        .filter { it.locale.language.equals("pt", ignoreCase = true) }
                        .sortedWith(compareByDescending<Voice> { it.quality }
                            .thenByDescending { !it.isNetworkConnectionRequired })
                        .firstOrNull()

                    if (bestVoice != null) {
                        ttsEngine.voice = bestVoice
                    }
                }
            } catch (_: Exception) {
                // Em dispositivos sem suporte dinâmico a voices, continua com o default
            }

            // Ajuste fino para dicção elegante, clara e profissional
            ttsEngine.setSpeechRate(0.95f) // Velocidade ligeiramente pausada para clareza
            ttsEngine.setPitch(1.02f)      // Tom harmônico e natural
            isInitialized = true
        }
    }

    /**
     * Interrompe qualquer áudio em andamento antes de falar
     */
    fun pararAudio() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    /**
     * Fala o texto fornecido garantindo que não repita a mesma fala em loop.
     */
    fun narrar(texto: String) {
        if (!isInitialized || tts == null || texto.isBlank()) {
            return
        }

        val textoLimpo = texto.trim()
        val now = System.currentTimeMillis()
        val ultimoDisparo = narracaoDebounceMap[textoLimpo] ?: 0L

        // Se a mesma mensagem foi enviada a menos de 6 segundos, ignora loop
        if (now - ultimoDisparo < NARRACAO_DEBOUNCE_MS) {
            return
        }
        narracaoDebounceMap[textoLimpo] = now

        // Limpeza periódica do mapa de debounce
        if (narracaoDebounceMap.size > 50) {
            val iter = narracaoDebounceMap.entries.iterator()
            while (iter.hasNext()) {
                val entry = iter.next()
                if (now - entry.value > 30_000L) {
                    iter.remove()
                }
            }
        }

        // QUEUE_FLUSH cancela o áudio anterior para não encavalar nem ficar falando sem parar
        tts?.speak(textoLimpo, TextToSpeech.QUEUE_FLUSH, null, "BL4CK_TTS_${System.currentTimeMillis()}")
    }

    /**
     * Formata um número como "84 123 4567" para que o sintetizador soe natural e não leia como bilhões
     */
    private fun formatarNumeroParaFala(numero: String): String {
        val limpo = numero.replace(Regex("[^0-9]"), "")
        if (limpo.length >= 8) {
            // Separa em blocos de dígitos para pronúncia pausada
            return limpo.chunked(2).joinToString(" ")
        }
        return limpo
    }

    /**
     * Pronunciamento inicial ao iniciar a transferência:
     * Narra apenas uma única vez no início do pedido.
     */
    fun narrarInicio(displayId: String, megas: String, numero: String) {
        val settings = AppSettingsManager.getInstance(context)
        if (settings.notificacoesGerais.value && settings.pronunciamentoInicial.value) {
            val megasLimpos = megas.replace(Regex("[^0-9]"), "").trim().ifBlank { megas }
            val numeroFalado = formatarNumeroParaFala(numero)
            val texto = "Iniciando envio de $megasLimpos megas para o número $numeroFalado"
            narrar(texto)
        }
    }

    /**
     * Pronunciamento final ao concluir a transferência:
     * Narra apenas uma única vez na conclusão do pedido.
     */
    fun narrarFim(displayId: String, megas: String, numero: String, sucesso: Boolean) {
        val settings = AppSettingsManager.getInstance(context)
        if (settings.notificacoesGerais.value && settings.pronunciamentoFinal.value) {
            val megasLimpos = megas.replace(Regex("[^0-9]"), "").trim().ifBlank { megas }
            val numeroFalado = formatarNumeroParaFala(numero)
            val texto = if (sucesso) {
                "Transferência de $megasLimpos megas para $numeroFalado concluída com sucesso"
            } else {
                "Transferência de $megasLimpos megas para $numeroFalado finalizada, requer análise"
            }
            narrar(texto)
        }
    }

    fun shutdown() {
        pararAudio()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    companion object {
        @Volatile
        private var instance: TtsPronunciadorHelper? = null

        fun getInstance(context: Context): TtsPronunciadorHelper {
            return instance ?: synchronized(this) {
                instance ?: TtsPronunciadorHelper(context).also { instance = it }
            }
        }
    }
}
