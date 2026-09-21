package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsPronunciadorHelper private constructor(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("pt", "MZ"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback para português do Brasil ou idioma padrão
                tts?.setLanguage(Locale("pt", "BR"))
            }
            tts?.setSpeechRate(1.0f)
            tts?.setPitch(1.0f)
            isInitialized = true
        }
    }

    fun narrar(texto: String) {
        if (!isInitialized || tts == null) {
            return
        }
        tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "BL4CK_TTS_${System.currentTimeMillis()}")
    }

    /**
     * Pronunciamento inicial ao iniciar a transferência:
     * Ex: "Enviando 1024MB para 84XXXXXXX"
     */
    fun narrarInicio(megas: String, numero: String) {
        val settings = AppSettingsManager.getInstance(context)
        if (settings.notificacoesGerais.value && settings.pronunciamentoInicial.value) {
            val megasLimpos = megas.replace(Regex("[^0-9]"), "").trim().ifBlank { megas }
            val texto = "Enviando $megasLimpos megas para $numero"
            narrar(texto)
        }
    }

    /**
     * Pronunciamento final ao concluir a transferência:
     * Ex: "Transferência de 1024MB para 84XXXXXXX Concluída"
     */
    fun narrarFim(megas: String, numero: String, sucesso: Boolean) {
        val settings = AppSettingsManager.getInstance(context)
        if (settings.notificacoesGerais.value && settings.pronunciamentoFinal.value) {
            val megasLimpos = megas.replace(Regex("[^0-9]"), "").trim().ifBlank { megas }
            val texto = if (sucesso) {
                "Transferência de $megasLimpos megas para $numero Concluída"
            } else {
                "Transferência de $megasLimpos megas para $numero requer análise"
            }
            narrar(texto)
        }
    }

    fun shutdown() {
        tts?.stop()
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
