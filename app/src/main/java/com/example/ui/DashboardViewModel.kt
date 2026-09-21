package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.Bl4ckDatabase
import com.example.data.model.HistoricoItem
import com.example.data.model.PedidoFila
import com.example.data.model.SimInfo
import com.example.network.ConnectionStatus
import com.example.network.ServidorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Bl4ckDatabase.getInstance(application)
    private val filaDao = db.pedidoFilaDao()
    private val historicoDao = db.historicoDao()
    val servidorManager = ServidorManager.getInstance(application)

    // Fila flows
    val pedidosFila: StateFlow<List<PedidoFila>> = filaDao.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFilaCount: StateFlow<Int> = filaDao.getTotalCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val aguardandoCount: StateFlow<Int> = filaDao.getAguardandoCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val emProcessamentoCount: StateFlow<Int> = filaDao.getEmProcessamentoCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Histórico flows
    val historicoList: StateFlow<List<HistoricoItem>> = historicoDao.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // SIM e Conexão flows
    val activeSim: StateFlow<Int> = servidorManager.activeSim
    val sim1Info: StateFlow<SimInfo> = servidorManager.sim1Info
    val sim2Info: StateFlow<SimInfo> = servidorManager.sim2Info
    val connectionStatus: StateFlow<ConnectionStatus> = servidorManager.connectionStatus
    val serverUrl: StateFlow<String> = servidorManager.serverUrl
    val lastLogMessage: StateFlow<String> = servidorManager.lastLogMessage

    // Motor de Execução Real (4-8s)
    val isEngineRunning: StateFlow<Boolean> = servidorManager.isEngineRunning
    val currentCountdown: StateFlow<Int> = servidorManager.currentCountdown
    val lastDispatchedUssd: StateFlow<String?> = servidorManager.lastDispatchedUssd

    // UI state
    private val _isScheduleModalOpen = MutableStateFlow(false)
    val isScheduleModalOpen: StateFlow<Boolean> = _isScheduleModalOpen.asStateFlow()

    private val _isEditLimitDialogOpen = MutableStateFlow(false)
    val isEditLimitDialogOpen: StateFlow<Boolean> = _isEditLimitDialogOpen.asStateFlow()

    private val _isDefinicoesModalOpen = MutableStateFlow(false)
    val isDefinicoesModalOpen: StateFlow<Boolean> = _isDefinicoesModalOpen.asStateFlow()

    fun openDefinicoesModal() {
        _isDefinicoesModalOpen.value = true
    }

    fun closeDefinicoesModal() {
        _isDefinicoesModalOpen.value = false
    }

    fun openScheduleModal() {
        _isScheduleModalOpen.value = true
    }

    fun closeScheduleModal() {
        _isScheduleModalOpen.value = false
    }

    fun openEditLimitDialog() {
        _isEditLimitDialogOpen.value = true
    }

    fun closeEditLimitDialog() {
        _isEditLimitDialogOpen.value = false
    }

    fun agendarTransferencia(numero: String, megas: String) {
        if (numero.isNotBlank() && megas.isNotBlank()) {
            servidorManager.adicionarPedidoFilaLocal(numero, megas)
            closeScheduleModal()
        }
    }

    fun processarProximo() {
        servidorManager.processarProximoDaFila()
    }

    fun limparFila() {
        servidorManager.limparFilaLocal()
    }

    fun alternarSim() {
        servidorManager.alternarSimAtivo()
    }

    fun atualizarSims() {
        servidorManager.atualizarInformacoesSims()
    }

    fun atualizarLimite(simSlot: Int, limite: Int, restantes: Int) {
        servidorManager.atualizarLimiteEnvios(simSlot, limite, restantes)
    }

    fun conectarWebSocket(url: String) {
        servidorManager.conectarWebSocket(url)
    }

    fun desconectarWebSocket() {
        servidorManager.desconectarWebSocket()
    }

    fun alternarConexao() {
        servidorManager.alternarEstadoConexao()
    }

    fun alternarMotorFila() {
        servidorManager.alternarMotorFila()
    }

    fun simularComandoRemoto(action: String) {
        val json = org.json.JSONObject().apply {
            put("action", action)
        }.toString()
        servidorManager.processarComandoRemoto(json)
    }

    fun excluirPedido(pedido: PedidoFila) {
        viewModelScope.launch {
            filaDao.delete(pedido)
        }
    }

    fun limparHistorico() {
        viewModelScope.launch {
            historicoDao.clearAll()
        }
    }
}
