package com.example.network

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.example.data.db.Bl4ckDatabase
import com.example.data.model.HistoricoItem
import com.example.data.model.PedidoFila
import com.example.data.model.SimCard
import com.example.data.model.SimInfo
import com.example.service.UssdAccessibilityService
import com.example.util.AppNotificationHelper
import com.example.util.SimCardHelper
import com.example.util.TtsPronunciadorHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

data class DeviceStatusReport(
    val activeSim: Int,
    val sim1Remaining: Int,
    val sim1Limit: Int,
    val sim2Remaining: Int,
    val sim2Limit: Int,
    val queueCount: Int
)

class ServidorManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val database = Bl4ckDatabase.getInstance(context)
    private val filaDao = database.pedidoFilaDao()
    private val historicoDao = database.historicoDao()
    private val simCardDao = database.simCardDao()

    val simCardsFlow = simCardDao.getAllFlow()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var webSocket: WebSocket? = null

    // Estados Reativos para a UI
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus = _connectionStatus.asStateFlow()

    private val _serverUrl = MutableStateFlow("ws://192.168.1.100:8080/ws/device")
    val serverUrl = _serverUrl.asStateFlow()

    private val _lastLogMessage = MutableStateFlow("Sistema Bl4ck pronto para operar.")
    val lastLogMessage = _lastLogMessage.asStateFlow()

    // SIM Cards Reais do Aparelho
    private val _activeSim = MutableStateFlow(0)
    val activeSim = _activeSim.asStateFlow()

    private val _sim1Info = MutableStateFlow(
        SimInfo(
            slot = 1,
            isActive = false,
            carrierName = "Sem cartões ativos",
            remainingSends = 10,
            totalLimit = 10,
            isInserted = false,
            isDefaultVoice = false
        )
    )
    val sim1Info = _sim1Info.asStateFlow()

    private val _sim2Info = MutableStateFlow(
        SimInfo(
            slot = 2,
            isActive = false,
            carrierName = "Sem cartões ativos",
            remainingSends = 10,
            totalLimit = 10,
            isInserted = false,
            isDefaultVoice = false
        )
    )
    val sim2Info = _sim2Info.asStateFlow()

    init {
        atualizarInformacoesSims()
    }

    // Motor USSD de Fila
    private val _isEngineRunning = MutableStateFlow(false)
    val isEngineRunning = _isEngineRunning.asStateFlow()

    private val _currentCountdown = MutableStateFlow(0)
    val currentCountdown = _currentCountdown.asStateFlow()

    private val _lastDispatchedUssd = MutableStateFlow("")
    val lastDispatchedUssd = _lastDispatchedUssd.asStateFlow()

    private var engineJob: Job? = null

    // -------------------------------------------------------------
    // MOTOR DE PROCESSAMENTO PRECISO PASSO A PASSO (*162#)
    // -------------------------------------------------------------

    fun iniciarMotorFila() {
        if (_isEngineRunning.value) return
        _isEngineRunning.value = true
        _lastLogMessage.value = "Motor Ativo: Modo preciso interativo (*162# -> 8 -> 2 -> Megas -> Número)."

        engineJob?.cancel()
        engineJob = scope.launch {
            while (_isEngineRunning.value) {
                val proximo = filaDao.getProximoAguardando()
                if (proximo == null) {
                    delay(2000)
                    continue
                }

                // Executa a automação passo a passo do pedido atual
                executarPedidoPassoAPasso(proximo)

                if (!_isEngineRunning.value) break

                // Intervalo seguro entre pedidos (4 a 8 segundos)
                val intervaloSegundos = Random.nextInt(4, 9)
                _lastLogMessage.value = "Aguardando ${intervaloSegundos}s para o próximo envio..."
                for (sec in intervaloSegundos downTo 1) {
                    if (!_isEngineRunning.value) break
                    _currentCountdown.value = sec
                    delay(1000)
                }
                _currentCountdown.value = 0
            }
        }
    }

    fun pararMotorFila() {
        _isEngineRunning.value = false
        _currentCountdown.value = 0
        engineJob?.cancel()
        engineJob = null
        UssdAccessibilityService.cancelarFluxo()
        _lastLogMessage.value = "Motor de envios pausado."
    }

    fun alternarMotorFila() {
        if (_isEngineRunning.value) {
            pararMotorFila()
        } else {
            iniciarMotorFila()
        }
    }

    /**
     * Limpa e formata a quantidade de megas para dígito puro (ex: "500", "1024")
     * 1GB corresponde a 1024MB
     */
    fun extrairMegasPuro(megas: String): String {
        return when {
            megas.contains("GB", ignoreCase = true) -> {
                val num = megas.replace(Regex("[^0-9.]"), "").trim().toDoubleOrNull() ?: 1.0
                (num * 1024).toInt().toString()
            }
            else -> {
                val digits = megas.replace(Regex("[^0-9]"), "").trim()
                if (digits.isNotBlank()) digits else "500"
            }
        }
    }

    /**
     * Limpa o número Vodacom MZ para o formato padrão local (84XXXXXXX ou 85XXXXXXX)
     */
    fun extrairNumeroVodacom(numero: String): String {
        var clean = numero.replace(Regex("[^0-9]"), "").trim()
        if (clean.startsWith("258") && clean.length > 9) {
            clean = clean.removePrefix("258")
        }
        return clean
    }

    /**
     * Dispara chamada USSD real no Android através de Intent.ACTION_CALL
     * Caso a permissão CALL_PHONE ainda não tenha sido concedida, utiliza o discador seguro ACTION_DIAL
     */
    fun dispararChamadaUssd(ussdCode: String, simSlot: Int): Boolean {
        val encodedHash = Uri.encode("#")
        val uriString = "tel:" + Uri.encode(ussdCode.replace("#", "")) + encodedHash

        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val targetSubId = if (simSlot == 1) _sim1Info.value.subscriptionId else _sim2Info.value.subscriptionId
        val targetCarrier = if (simSlot == 1) _sim1Info.value.carrierName else _sim2Info.value.carrierName

        return if (hasCallPermission) {
            try {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse(uriString)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("com.android.phone.extra.slot", simSlot - 1)
                    putExtra("simSlot", simSlot - 1)
                    putExtra("slot", simSlot - 1)
                    putExtra("phone_type", simSlot)
                    putExtra("Subscription", targetSubId)
                    putExtra("subscription", targetSubId)
                    putExtra("android.telephony.extra.SUBSCRIPTION_INDEX", targetSubId)
                    putExtra("android.telephony.extra.SLOT_INDEX", simSlot - 1)
                }
                context.startActivity(intent)
                _lastDispatchedUssd.value = ussdCode
                _lastLogMessage.value = "Disparando $ussdCode via SIM $simSlot ($targetCarrier)..."
                true
            } catch (e: SecurityException) {
                fallbackActionDial(uriString, ussdCode, simSlot)
            } catch (e: Exception) {
                _lastLogMessage.value = "Erro ao disparar chamada: ${e.message}"
                false
            }
        } else {
            _lastLogMessage.value = "⚠️ Permissão de chamada necessária. Abrindo discador com $ussdCode..."
            fallbackActionDial(uriString, ussdCode, simSlot)
        }
    }

    private fun fallbackActionDial(uriString: String, ussdCode: String, simSlot: Int): Boolean {
        return try {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse(uriString)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("com.android.phone.extra.slot", simSlot - 1)
                putExtra("simSlot", simSlot - 1)
                putExtra("android.telephony.extra.SLOT_INDEX", simSlot - 1)
            }
            context.startActivity(dialIntent)
            _lastDispatchedUssd.value = ussdCode
            true
        } catch (e: Exception) {
            _lastLogMessage.value = "Erro ao abrir discador: ${e.message}"
            false
        }
    }

    /**
     * Limpa o texto da resposta da operadora para conter unicamente
     * a resposta da operadora, eliminando artefatos, múltiplos passos e botões de interface.
     * No histórico e relatórios a resposta nunca deve ter mais de 70 caracteres (corta o resto).
     */
    fun limparRespostaOperadora(textoBruto: String): String {
        var limpo = textoBruto.trim()
        if (limpo.contains("Etapa") || limpo.contains("Step")) {
            limpo = limpo.substringAfterLast(":").trim()
        }
        val buttonRegex = Regex("(?i)\\b(ok|fechar|close|cancelar|send|enviar|dismiss|entendido)\\b")
        limpo = limpo.replace(buttonRegex, "").trim()
        limpo = limpo.replace(Regex("\\s+"), " ").trim()
        // No histórico a parte da resposta da operadora nunca deve ter mais de 70 caracteres
        if (limpo.length > 70) {
            limpo = limpo.take(70).trim()
        }
        return limpo
    }

    /**
     * Verifica se a resposta recebida é a resposta padrão de sucesso da operadora Vodacom
     * Resposta padrão esperada: "Transferiste com sucesso 1024MB etc"
     * Se for diferente dessa resposta padrão de sucesso, deve vir com a tag ANÁLISE no histórico.
     */
    fun isRespostaSucessoPadrao(texto: String): Boolean {
        val t = texto.lowercase().trim()
        return t.contains("transferiste com sucesso") ||
                t.contains("transferido com sucesso") ||
                (t.contains("com sucesso") && (t.contains("transfer") || t.contains("mb")))
    }

    /**
     * Automação Precisa Solicitada:
     * 1. Limpa e fecha qualquer popup/menu ativo na tela antes de executar
     * 2. Prepara acessibilidade para aguardar menus interativos
     * 3. Dispara *162#
     * 4. Acessibilidade espera e digita 8
     * 5. Espera e digita 2
     * 6. Espera e digita os Megas
     * 7. Espera e digita o Número
     * 8. Lê exclusivamente a resposta da operadora que vem depois de colocar o número
     * 9. Executa OK para fechar o diálogo final da operadora
     * 10. Registra no histórico: se a resposta for padrão sucesso ("Transferiste com sucesso..."),
     *     salva CONCLUÍDO; se for diferente, classifica como ANÁLISE.
     */
    suspend fun executarPedidoPassoAPasso(pedido: PedidoFila) {
        val currentSimSlot = _activeSim.value
        val currentSim = if (currentSimSlot == 1) _sim1Info.value else _sim2Info.value

        if (currentSim.isLimitReached) {
            _lastLogMessage.value = "⚠️ Limite atingido para SIM $currentSimSlot. Pedido ${pedido.displayId} pausado."
            filaDao.update(pedido.copy(status = "FALHA"))
            AppNotificationHelper.getInstance(context).notificarErro(
                titulo = "Limite Atingido",
                mensagem = "Limite do SIM $currentSimSlot atingido. Pedido ${pedido.displayId} pausado."
            )
            return
        }

        val cleanMegas = extrairMegasPuro(pedido.megas)
        val cleanNumero = extrairNumeroVodacom(pedido.numeroDestino)

        // Pronunciamento inicial (ex: "Enviando 1024MB para 84XXXXXXX") e notificação
        AppNotificationHelper.getInstance(context).notificarIniciarTransferencia(
            displayId = pedido.displayId,
            megas = cleanMegas,
            numero = cleanNumero
        )
        TtsPronunciadorHelper.getInstance(context).narrarInicio(
            displayId = pedido.displayId,
            megas = cleanMegas,
            numero = cleanNumero
        )

        // Marca como EM PROCESSAMENTO
        filaDao.update(pedido.copy(status = "EM PROCESSAMENTO"))
        _lastLogMessage.value = "Iniciando transferência para ${pedido.displayId} ($cleanMegas MB -> $cleanNumero)..."

        // 1. Fecha qualquer popup ou menu ativo na tela antes de executar
        UssdAccessibilityService.fecharPopupsAtivos()
        delay(400)

        // Arma o UssdAccessibilityService para navegar no menu interativo
        UssdAccessibilityService.iniciarFluxoPreciso(
            megas = cleanMegas,
            numero = cleanNumero
        )

        // Dispara *162# no modem telefônico
        val sucessoDisparo = dispararChamadaUssd("*162#", currentSimSlot)

        // Decrementa cota do SIM ativo
        if (currentSimSlot == 1) {
            _sim1Info.value = _sim1Info.value.copy(remainingSends = (_sim1Info.value.remainingSends - 1).coerceAtLeast(0))
        } else {
            _sim2Info.value = _sim2Info.value.copy(remainingSends = (_sim2Info.value.remainingSends - 1).coerceAtLeast(0))
        }

        // Aguarda a progressão dos passos interativos e a captura da resposta final (ou timeout de 20s)
        var segundosAguardando = 0
        val maxEspera = 20
        while (UssdAccessibilityService.currentActiveStep.value != UssdAccessibilityService.Step.IDLE && segundosAguardando < maxEspera) {
            delay(1000)
            segundosAguardando++
        }

        val timeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val now = System.currentTimeMillis()

        // Obtém apenas a resposta limpa e direta retornada pela operadora
        val respostaBruta = UssdAccessibilityService.getUltimaRespostaOperadora()
        val respostaLimpa = limparRespostaOperadora(respostaBruta).ifBlank {
            if (sucessoDisparo) "Aguardando confirmação da operadora" else "Falha ao discar *162#"
        }

        // Se a resposta for padrão ("Transferiste com sucesso..."), CONCLUIDO.
        // Se for diferente dessa, classifica com a tag ANALISE.
        val isSucesso = isRespostaSucessoPadrao(respostaLimpa)
        val statusFinal = when {
            !sucessoDisparo -> "FALHA"
            isSucesso -> "CONCLUIDO"
            else -> "ANALISE"
        }

        val historico = HistoricoItem(
            displayId = pedido.displayId,
            numeroDestino = pedido.numeroDestino,
            megas = pedido.megas,
            status = statusFinal,
            timestamp = now,
            formattedTime = timeFormat.format(Date(now)),
            ussdResposta = respostaLimpa,
            simSlot = currentSimSlot
        )

        historicoDao.insert(historico)
        filaDao.delete(pedido)
        _lastLogMessage.value = "Pedido ${pedido.displayId} processado: $statusFinal"
        enviarRelatorioEstado()

        // Pronunciamento final (ex: "Transferência de 1024MB para 84XXXXXXX Concluída") e notificação
        AppNotificationHelper.getInstance(context).notificarFinalizarTransferencia(
            displayId = pedido.displayId,
            megas = cleanMegas,
            numero = cleanNumero,
            resposta = respostaLimpa,
            status = statusFinal
        )
        TtsPronunciadorHelper.getInstance(context).narrarFim(
            displayId = pedido.displayId,
            megas = cleanMegas,
            numero = cleanNumero,
            sucesso = isSucesso
        )
    }

    fun processarProximoDaFila() {
        scope.launch {
            val proximo = filaDao.getProximoAguardando()
            if (proximo == null) {
                _lastLogMessage.value = "Nenhum pedido aguardando processamento na fila."
                return@launch
            }
            executarPedidoPassoAPasso(proximo)
        }
    }

    // -------------------------------------------------------------
    // OPERAÇÕES DA FILA & REGISTRO LOCAL
    // -------------------------------------------------------------

    fun adicionarPedidoFilaLocal(numero: String, megas: String): PedidoFila {
        val nextId = "#BLK-" + (1000 + (System.currentTimeMillis() % 9000).toInt())
        val slot = _activeSim.value
        val pedido = PedidoFila(
            displayId = nextId,
            numeroDestino = numero.trim(),
            megas = megas.trim(),
            status = "AGUARDANDO",
            simSlot = slot
        )
        scope.launch {
            filaDao.insert(pedido)
            _lastLogMessage.value = "Novo pedido $nextId adicionado ($megas para $numero)."
            AppNotificationHelper.getInstance(context).notificarReceberPedido(
                displayId = nextId,
                megas = megas,
                numero = numero
            )
            enviarRelatorioEstado()
        }
        return pedido
    }

    suspend fun buscarHistorico(): List<HistoricoItem> {
        return historicoDao.getAll()
    }

    fun limparFilaLocal() {
        scope.launch {
            filaDao.clearAll()
            _lastLogMessage.value = "Fila limpa com sucesso."
            enviarRelatorioEstado()
        }
    }

    fun limparHistoricoLocal() {
        scope.launch {
            historicoDao.clearAll()
            _lastLogMessage.value = "Histórico limpo com sucesso."
        }
    }

    fun atualizarInformacoesSims() {
        scope.launch {
            val dbSims = simCardDao.getAll().associateBy { it.slot }
            val resolvedList = SimCardHelper.resolverEstadoSimsCompletos(context, dbSims)
            simCardDao.insertAll(resolvedList)

            val s1 = resolvedList.firstOrNull { it.slot == 1 }
            val s2 = resolvedList.firstOrNull { it.slot == 2 }

            if (s1 != null) {
                _sim1Info.value = SimInfo(
                    slot = 1,
                    isActive = s1.isActiveVoice,
                    carrierName = if (s1.isInserted) s1.providerName else "Sem cartões ativos",
                    remainingSends = s1.remainingSends,
                    totalLimit = s1.totalLimit,
                    isInserted = s1.isInserted,
                    isDefaultVoice = s1.isActiveVoice,
                    subscriptionId = s1.subscriptionId
                )
            }
            if (s2 != null) {
                _sim2Info.value = SimInfo(
                    slot = 2,
                    isActive = s2.isActiveVoice,
                    carrierName = if (s2.isInserted) s2.providerName else "Sem cartões ativos",
                    remainingSends = s2.remainingSends,
                    totalLimit = s2.totalLimit,
                    isInserted = s2.isInserted,
                    isDefaultVoice = s2.isActiveVoice,
                    subscriptionId = s2.subscriptionId
                )
            }

            _activeSim.value = when {
                s1?.isActiveVoice == true -> 1
                s2?.isActiveVoice == true -> 2
                s1?.isInserted == true -> 1
                s2?.isInserted == true -> 2
                else -> 0
            }
        }
    }

    fun alternarSimAtivo(novoSlot: Int? = null, abrirConfiguracoes: Boolean = true) {
        val targetSlot = novoSlot ?: if (_activeSim.value == 1) 2 else 1
        if (targetSlot == 2 && !_sim2Info.value.isInserted) {
            _lastLogMessage.value = "Aparelho possui apenas 1 SIM card ativo (${_sim1Info.value.carrierName})."
            return
        }

        _activeSim.value = targetSlot
        _sim1Info.value = _sim1Info.value.copy(
            isActive = targetSlot == 1,
            isDefaultVoice = targetSlot == 1
        )
        _sim2Info.value = _sim2Info.value.copy(
            isActive = targetSlot == 2,
            isDefaultVoice = targetSlot == 2
        )
        scope.launch {
            simCardDao.setActiveVoiceSlot(targetSlot)
        }
        val carrierName = if (targetSlot == 1) _sim1Info.value.carrierName else _sim2Info.value.carrierName
        _lastLogMessage.value = "SIM ativo alterado para SIM $targetSlot ($carrierName)."
        enviarRelatorioEstado()

        if (abrirConfiguracoes && _sim2Info.value.isInserted) {
            SimCardHelper.abrirConfiguracoesSim(context)
        }
    }

    fun atualizarNumeroTelefoneSim(simSlot: Int, novoNumero: String) {
        scope.launch {
            simCardDao.updatePhoneNumber(simSlot, novoNumero.trim())
            _lastLogMessage.value = "Número do SIM $simSlot atualizado: $novoNumero"
            atualizarInformacoesSims()
        }
    }

    fun atualizarLimiteEnvios(simSlot: Int, novoLimite: Int, restantes: Int) {
        val finalRestantes = restantes.coerceAtMost(novoLimite)
        if (simSlot == 1) {
            _sim1Info.value = _sim1Info.value.copy(
                totalLimit = novoLimite,
                remainingSends = finalRestantes
            )
        } else {
            _sim2Info.value = _sim2Info.value.copy(
                totalLimit = novoLimite,
                remainingSends = finalRestantes
            )
        }
        scope.launch {
            val existing = simCardDao.getBySlot(simSlot)
            if (existing != null) {
                simCardDao.insertOrUpdate(existing.copy(
                    totalLimit = novoLimite,
                    remainingSends = finalRestantes
                ))
            }
        }
        _lastLogMessage.value = "Limite atualizado para SIM $simSlot: $restantes/$novoLimite restantes."
        enviarRelatorioEstado()
    }

    suspend fun obterRelatorioEstado(): DeviceStatusReport {
        val totalFila = filaDao.getAll().size
        return DeviceStatusReport(
            activeSim = _activeSim.value,
            sim1Remaining = _sim1Info.value.remainingSends,
            sim1Limit = _sim1Info.value.totalLimit,
            sim2Remaining = _sim2Info.value.remainingSends,
            sim2Limit = _sim2Info.value.totalLimit,
            queueCount = totalFila
        )
    }

    /**
     * Registra resposta final capturada pelo UssdAccessibilityService
     */
    fun registrarRespostaUssd(texto: String) {
        val respostaLimpa = limparRespostaOperadora(texto)
        if (respostaLimpa.isBlank()) return

        scope.launch {
            _lastLogMessage.value = "Resposta da operadora: $respostaLimpa"

            // Atualiza o registro de histórico mais recente com a resposta pura da operadora
            val ultimos = historicoDao.getAll()
            if (ultimos.isNotEmpty()) {
                val recente = ultimos.first()
                val isSucesso = isRespostaSucessoPadrao(respostaLimpa)
                val statusFinal = if (isSucesso) "CONCLUIDO" else "ANALISE"

                // Atualiza o registro se o status ou resposta final tiver mudado
                if (recente.ussdResposta != respostaLimpa || recente.status != statusFinal) {
                    historicoDao.update(recente.copy(ussdResposta = respostaLimpa, status = statusFinal))
                }
            }
        }
    }

    // -------------------------------------------------------------
    // COMUNICAÇÃO COM O BOT NODE.JS (WEBSOCKET / HTTP)
    // -------------------------------------------------------------

    fun conectarWebSocket(url: String = _serverUrl.value) {
        _serverUrl.value = url
        _connectionStatus.value = ConnectionStatus.CONNECTING
        _lastLogMessage.value = "Conectando ao bot em $url..."

        try {
            webSocket?.close(1000, "Reconectando")
            val request = Request.Builder().url(url).build()
            webSocket = okHttpClient.newWebSocket(request, createWebSocketListener())
        } catch (e: Exception) {
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            _lastLogMessage.value = "Falha de conexão com o servidor: ${e.message}"
        }
    }

    fun desconectarWebSocket() {
        webSocket?.close(1000, "Desconectado pelo usuário")
        webSocket = null
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        _lastLogMessage.value = "Desconectado do bot."
    }

    fun alternarEstadoConexao() {
        if (_connectionStatus.value == ConnectionStatus.CONNECTED) {
            desconectarWebSocket()
        } else {
            conectarWebSocket()
        }
    }

    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionStatus.value = ConnectionStatus.CONNECTED
                _lastLogMessage.value = "🟢 Conectado ao bot WhatsApp com sucesso."
                enviarRelatorioEstado()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                _lastLogMessage.value = "Comando recebido: $text"
                processarComandoRemoto(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                _lastLogMessage.value = "Conexão encerrada pelo servidor."
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                _lastLogMessage.value = "Erro na conexão: ${t.message}"
                AppNotificationHelper.getInstance(context).notificarErro(
                    titulo = "Erro de Conexão",
                    mensagem = "Falha ao conectar com o bot: ${t.message}"
                )
            }
        }
    }

    fun processarComandoRemoto(jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            val action = json.optString("action", "")

            when (action) {
                "NEW_TRANSFER", "ADD_QUEUE" -> {
                    val numero = json.optString("numero", json.optString("destinatario", ""))
                    val megas = json.optString("megas", json.optString("quantidade", "500 MB"))
                    if (numero.isNotBlank()) {
                        adicionarPedidoFilaLocal(numero, megas)
                    }
                }

                "SWITCH_SIM" -> {
                    val targetSlot = json.optInt("slot", 0)
                    if (targetSlot in 1..2) {
                        alternarSimAtivo(targetSlot)
                    } else {
                        alternarSimAtivo()
                    }
                }

                "START_ENGINE" -> iniciarMotorFila()
                "STOP_ENGINE" -> pararMotorFila()
                "CLEAR_QUEUE" -> limparFilaLocal()

                "UPDATE_LIMITS" -> {
                    val slot = json.optInt("slot", 1)
                    val limit = json.optInt("totalLimit", 10)
                    val remaining = json.optInt("remainingSends", limit)
                    atualizarLimiteEnvios(slot, limit, remaining)
                }

                "GET_STATUS" -> enviarRelatorioEstado()
            }
        } catch (e: Exception) {
            // Silencioso
        }
    }

    fun enviarRelatorioEstado() {
        scope.launch {
            if (_connectionStatus.value != ConnectionStatus.CONNECTED || webSocket == null) return@launch
            try {
                val relatorio = obterRelatorioEstado()
                val json = JSONObject().apply {
                    put("event", "DEVICE_STATUS")
                    put("activeSim", relatorio.activeSim)
                    put("sim1Remaining", relatorio.sim1Remaining)
                    put("sim1Limit", relatorio.sim1Limit)
                    put("sim2Remaining", relatorio.sim2Remaining)
                    put("sim2Limit", relatorio.sim2Limit)
                    put("queueCount", relatorio.queueCount)
                    put("isEngineRunning", _isEngineRunning.value)
                    put("timestamp", System.currentTimeMillis())
                }
                webSocket?.send(json.toString())
            } catch (e: Exception) {
                // Silencioso
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ServidorManager? = null

        fun getInstance(context: Context): ServidorManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ServidorManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
