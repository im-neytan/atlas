package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettingsManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _notificacoesGerais = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICACOES_GERAIS, true))
    val notificacoesGerais: StateFlow<Boolean> = _notificacoesGerais.asStateFlow()

    private val _notificarReceberPedido = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICAR_RECEBER_PEDIDO, true))
    val notificarReceberPedido: StateFlow<Boolean> = _notificarReceberPedido.asStateFlow()

    private val _notificarIniciarTransferencia = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICAR_INICIAR_TRANSFERENCIA, true))
    val notificarIniciarTransferencia: StateFlow<Boolean> = _notificarIniciarTransferencia.asStateFlow()

    private val _notificarFinalizarTransferencia = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICAR_FINALIZAR_TRANSFERENCIA, true))
    val notificarFinalizarTransferencia: StateFlow<Boolean> = _notificarFinalizarTransferencia.asStateFlow()

    private val _notificarErro = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICAR_ERRO, true))
    val notificarErro: StateFlow<Boolean> = _notificarErro.asStateFlow()

    private val _pronunciamentoInicial = MutableStateFlow(prefs.getBoolean(KEY_PRONUNCIAMENTO_INICIAL, true))
    val pronunciamentoInicial: StateFlow<Boolean> = _pronunciamentoInicial.asStateFlow()

    private val _pronunciamentoFinal = MutableStateFlow(prefs.getBoolean(KEY_PRONUNCIAMENTO_FINAL, true))
    val pronunciamentoFinal: StateFlow<Boolean> = _pronunciamentoFinal.asStateFlow()

    fun setNotificacoesGerais(ativo: Boolean) {
        _notificacoesGerais.value = ativo
        prefs.edit().putBoolean(KEY_NOTIFICACOES_GERAIS, ativo).apply()
    }

    fun setNotificarReceberPedido(ativo: Boolean) {
        _notificarReceberPedido.value = ativo
        prefs.edit().putBoolean(KEY_NOTIFICAR_RECEBER_PEDIDO, ativo).apply()
    }

    fun setNotificarIniciarTransferencia(ativo: Boolean) {
        _notificarIniciarTransferencia.value = ativo
        prefs.edit().putBoolean(KEY_NOTIFICAR_INICIAR_TRANSFERENCIA, ativo).apply()
    }

    fun setNotificarFinalizarTransferencia(ativo: Boolean) {
        _notificarFinalizarTransferencia.value = ativo
        prefs.edit().putBoolean(KEY_NOTIFICAR_FINALIZAR_TRANSFERENCIA, ativo).apply()
    }

    fun setNotificarErro(ativo: Boolean) {
        _notificarErro.value = ativo
        prefs.edit().putBoolean(KEY_NOTIFICAR_ERRO, ativo).apply()
    }

    fun setPronunciamentoInicial(ativo: Boolean) {
        _pronunciamentoInicial.value = ativo
        prefs.edit().putBoolean(KEY_PRONUNCIAMENTO_INICIAL, ativo).apply()
    }

    fun setPronunciamentoFinal(ativo: Boolean) {
        _pronunciamentoFinal.value = ativo
        prefs.edit().putBoolean(KEY_PRONUNCIAMENTO_FINAL, ativo).apply()
    }

    companion object {
        private const val PREFS_NAME = "bl4ck_system_settings"

        private const val KEY_NOTIFICACOES_GERAIS = "pref_notificacoes_gerais"
        private const val KEY_NOTIFICAR_RECEBER_PEDIDO = "pref_notificar_receber_pedido"
        private const val KEY_NOTIFICAR_INICIAR_TRANSFERENCIA = "pref_notificar_iniciar_transferencia"
        private const val KEY_NOTIFICAR_FINALIZAR_TRANSFERENCIA = "pref_notificar_finalizar_transferencia"
        private const val KEY_NOTIFICAR_ERRO = "pref_notificar_erro"
        private const val KEY_PRONUNCIAMENTO_INICIAL = "pref_pronunciamento_inicial"
        private const val KEY_PRONUNCIAMENTO_FINAL = "pref_pronunciamento_final"

        @Volatile
        private var instance: AppSettingsManager? = null

        fun getInstance(context: Context): AppSettingsManager {
            return instance ?: synchronized(this) {
                instance ?: AppSettingsManager(context).also { instance = it }
            }
        }
    }
}
