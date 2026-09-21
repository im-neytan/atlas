package com.example.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import com.example.network.ServidorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Registro de captura de conteúdo e resposta em cada etapa do menu USSD.
 */
data class StepCapture(
    val step: UssdAccessibilityService.Step,
    val stepIndex: Int,
    val screenContent: String,
    val inputSent: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Serviço de Acessibilidade para automação e leitura de menus USSD interativos.
 * Captura o conteúdo de tela a cada etapa e envia as opções do fluxo *162#:
 *  - Passo 1: Captura menu após *162# e responde "8"
 *  - Passo 2: Captura submenu e responde "2"
 *  - Passo 3: Captura solicitação de megas e envia o volume desejado
 *  - Passo 4: Captura solicitação de número destinatário e envia o número Vodacom
 *  - Passo 5: Captura a resposta final da operadora e fecha o diálogo com sucesso.
 */
class UssdAccessibilityService : AccessibilityService() {

    enum class Step(val label: String) {
        IDLE("Inativo"),
        WAITING_STEP_8("Aguardando Menu Principal (*162#)"),
        WAITING_STEP_2("Aguardando Submenu (Opção 2)"),
        WAITING_STEP_MEGAS("Aguardando Campo de Megas"),
        WAITING_STEP_NUMERO("Aguardando Campo de Número"),
        WAITING_FINAL_RESPONSE("Aguardando Resposta Final")
    }

    interface OnStepCapturedListener {
        fun onStepCaptured(capture: StepCapture)
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var lastHandledText: String = ""

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        try {
            val eventType = event.eventType
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            ) {
                val rootNode = rootInActiveWindow ?: event.source ?: return
                val capturedText = extractTextFromNode(rootNode)

                if (capturedText.isBlank()) return

                val currentStep = _currentActiveStep.value

                // Se o texto na tela for idêntico ao já manipulado neste passo, ignora repetição
                if (capturedText == lastHandledText && currentStep != Step.WAITING_FINAL_RESPONSE) {
                    return
                }

                _lastCapturedScreenText.value = capturedText

                // Modo passivo: se não estiver em automação de fluxo ativo, apenas registra diálogos USSD avulsos
                if (currentStep == Step.IDLE) {
                    if (isUssdDialog(rootNode, capturedText)) {
                        ServidorManager.getInstance(applicationContext).registrarRespostaUssd(capturedText)
                    }
                    return
                }

                // Executa a transição da etapa atual e registra o conteúdo lido
                handleAutomatedStep(rootNode, capturedText, currentStep)
            }
        } catch (e: Exception) {
            // Ignora silenciosamente
        }
    }

    /**
     * Gerencia a leitura da tela atual e a injeção do próximo dado no fluxo *162#
     */
    private fun handleAutomatedStep(
        rootNode: AccessibilityNodeInfo,
        capturedText: String,
        currentStep: Step
    ) {
        val inputNode = findInputEditText(rootNode)
        val sendButton = findSendButton(rootNode)

        when (currentStep) {
            Step.WAITING_STEP_8 -> {
                if (inputNode != null) {
                    lastHandledText = capturedText
                    recordStepCapture(Step.WAITING_STEP_8, 1, capturedText, "8")
                    _currentActiveStep.value = Step.WAITING_STEP_2
                    _stepLogFlow.value = "Etapa 1/4 concluída: Resposta lida. Selecionando opção 8..."

                    serviceScope.launch {
                        delay(600) // Delay tático para estabilização visual da tela
                        sendInputText(inputNode, "8", sendButton)
                    }
                }
            }

            Step.WAITING_STEP_2 -> {
                if (inputNode != null) {
                    lastHandledText = capturedText
                    recordStepCapture(Step.WAITING_STEP_2, 2, capturedText, "2")
                    _currentActiveStep.value = Step.WAITING_STEP_MEGAS
                    _stepLogFlow.value = "Etapa 2/4 concluída: Resposta lida. Selecionando opção 2..."

                    serviceScope.launch {
                        delay(600)
                        sendInputText(inputNode, "2", sendButton)
                    }
                }
            }

            Step.WAITING_STEP_MEGAS -> {
                if (inputNode != null) {
                    val megas = targetMegas.value
                    lastHandledText = capturedText
                    recordStepCapture(Step.WAITING_STEP_MEGAS, 3, capturedText, megas)
                    _currentActiveStep.value = Step.WAITING_STEP_NUMERO
                    _stepLogFlow.value = "Etapa 3/4 concluída: Solicitado Megas. Inserindo $megas MB..."

                    serviceScope.launch {
                        delay(600)
                        sendInputText(inputNode, megas, sendButton)
                    }
                }
            }

            Step.WAITING_STEP_NUMERO -> {
                if (inputNode != null) {
                    val numero = targetNumero.value
                    lastHandledText = capturedText
                    recordStepCapture(Step.WAITING_STEP_NUMERO, 4, capturedText, numero)
                    _currentActiveStep.value = Step.WAITING_FINAL_RESPONSE
                    _stepLogFlow.value = "Etapa 4/4 concluída: Solicitado Destinatário. Inserindo $numero..."

                    serviceScope.launch {
                        delay(600)
                        sendInputText(inputNode, numero, sendButton)
                    }
                }
            }

            Step.WAITING_FINAL_RESPONSE -> {
                // Captura a mensagem final limpa de confirmação / débito / erro emitida pela operadora
                val pureResponse = extractPureOperatorResponse(rootNode).ifBlank { capturedText }
                lastHandledText = capturedText
                recordStepCapture(Step.WAITING_FINAL_RESPONSE, 5, pureResponse, "[FIM DO FLUXO]")
                _ultimaRespostaFinal.value = pureResponse
                _stepLogFlow.value = "Concluído: $pureResponse"
                _currentActiveStep.value = Step.IDLE

                ServidorManager.getInstance(applicationContext).registrarRespostaUssd(pureResponse)

                // Clica no botão OK/Fechar do diálogo da operadora para dispensar a janela
                serviceScope.launch {
                    delay(300)
                    findAndClickDismissButton(rootNode)
                }
            }

            Step.IDLE -> {
                // Aguardando novas ações
            }
        }
    }

    /**
     * Registra o conteúdo da tela lido nesta etapa no histórico de capturas
     */
    private fun recordStepCapture(step: Step, index: Int, screenText: String, inputSent: String) {
        val capture = StepCapture(
            step = step,
            stepIndex = index,
            screenContent = screenText,
            inputSent = inputSent,
            timestamp = System.currentTimeMillis()
        )
        val currentList = _stepCapturesHistory.value.toMutableList()
        currentList.add(capture)
        _stepCapturesHistory.value = currentList

        // Notifica listeners registrados
        listeners.forEach { listener ->
            try {
                listener.onStepCaptured(capture)
            } catch (e: Exception) {
                // Silencioso
            }
        }
    }

    /**
     * Injeta texto no campo de formulário USSD e aciona o botão de envio
     * Utiliza estritamente APIs públicas de acessibilidade, sem invocar métodos bloqueados
     */
    private fun sendInputText(
        inputNode: AccessibilityNodeInfo,
        text: String,
        sendButton: AccessibilityNodeInfo?
    ) {
        try {
            // Garante que o nó receba foco antes da injeção
            if (!inputNode.isFocused) {
                inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            }

            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            val textSet = inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

            if (!textSet) {
                // Tentativa secundária com foco
                inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            }

            // Aciona o botão de Envio se disponível; caso contrário aciona clique no input
            if (sendButton != null) {
                val clicked = clickNodeOrParent(sendButton)
                if (!clicked) {
                    sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            } else {
                inputNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        } catch (e: Exception) {
            // Silencioso
        }
    }

    private fun clickNodeOrParent(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            current = current.parent
        }
        return false
    }

    /**
     * Localiza o campo EditText interativo na árvore de acessibilidade
     */
    private fun findInputEditText(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.className?.toString() == EditText::class.java.name ||
            node.className?.toString()?.contains("EditText", ignoreCase = true) == true ||
            node.isEditable
        ) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findInputEditText(child)
            if (found != null) return found
        }
        return null
    }

    /**
     * Localiza o botão Enviar/Send/OK/Responder na árvore de acessibilidade
     */
    private fun findSendButton(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val sendLabels = listOf("enviar", "send", "ok", "responder", "reply", "submeter", "continuar")
        val nodeText = node.text?.toString()?.trim()?.lowercase() ?: ""

        if (node.isClickable && sendLabels.any { nodeText == it || nodeText.contains(it) }) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findSendButton(child)
            if (found != null) return found
        }
        return null
    }

    /**
     * Localiza e clica no botão de descarte/fechamento do diálogo da operadora
     */
    private fun findAndClickDismissButton(node: AccessibilityNodeInfo): Boolean {
        val dismissLabels = listOf("ok", "fechar", "close", "entendido", "dismiss", "cancelar", "aceitar", "concluido")
        val nodeText = node.text?.toString()?.trim()?.lowercase() ?: ""

        if (dismissLabels.any { nodeText == it }) {
            if (clickNodeOrParent(node)) return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (findAndClickDismissButton(child)) return true
        }
        return false
    }

    /**
     * Determina se uma janela exibida na tela é um diálogo de rede celular/USSD
     */
    private fun isUssdDialog(node: AccessibilityNodeInfo, text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("vodacom") ||
                lower.contains("transfer") ||
                lower.contains("saldo") ||
                lower.contains("sucesso") ||
                lower.contains("megas") ||
                lower.contains("mb") ||
                lower.contains("162") ||
                lower.contains("erro") ||
                findInputEditText(node) != null
    }

    /**
     * Extrai puramente a resposta textual da operadora, filtrando botões do diálogo (OK, Cancelar, etc.)
     */
    private fun extractPureOperatorResponse(rootNode: AccessibilityNodeInfo): String {
        val texts = mutableListOf<String>()
        val buttonLabels = setOf("ok", "fechar", "close", "cancelar", "dismiss", "entendido", "enviar", "send", "resposta", "responder", "concluido", "aceitar")

        fun collectTexts(node: AccessibilityNodeInfo) {
            val text = node.text?.toString()?.trim()
            val isButton = node.isClickable || buttonLabels.contains(text?.lowercase())
            if (!text.isNullOrBlank() && !isButton) {
                if (!buttonLabels.contains(text.lowercase())) {
                    texts.add(text)
                }
            }
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                collectTexts(child)
            }
        }

        collectTexts(rootNode)
        val full = texts.joinToString(" ").trim()
        return full.ifBlank { extractTextFromNode(rootNode) }
    }

    /**
     * Extrai todo o conteúdo textual dos nós da tela de forma recursiva
     */
    private fun extractTextFromNode(node: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        val text = node.text
        if (!text.isNullOrBlank()) {
            sb.append(text).append(" ")
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                sb.append(extractTextFromNode(child))
            }
        }
        return sb.toString().trim()
    }

    override fun onInterrupt() {
        _currentActiveStep.value = Step.IDLE
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        activeInstance = this
        _isServiceBound.value = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activeInstance == this) {
            activeInstance = null
        }
        _isServiceBound.value = false
    }

    companion object {
        private const val TAG = "UssdAutomator"
        private var activeInstance: UssdAccessibilityService? = null

        private val _isServiceBound = MutableStateFlow(false)
        val isServiceBound = _isServiceBound.asStateFlow()

        private val _currentActiveStep = MutableStateFlow(Step.IDLE)
        val currentActiveStep = _currentActiveStep.asStateFlow()

        private val _stepLogFlow = MutableStateFlow("Pronto para execução.")
        val stepLogFlow = _stepLogFlow.asStateFlow()

        private val _lastCapturedScreenText = MutableStateFlow("")
        val lastCapturedScreenText = _lastCapturedScreenText.asStateFlow()

        private val _ultimaRespostaFinal = MutableStateFlow("")
        val ultimaRespostaFinal = _ultimaRespostaFinal.asStateFlow()

        private val _stepCapturesHistory = MutableStateFlow<List<StepCapture>>(emptyList())
        val stepCapturesHistory = _stepCapturesHistory.asStateFlow()

        val targetMegas = MutableStateFlow("500")
        val targetNumero = MutableStateFlow("")

        private val listeners = CopyOnWriteArrayList<OnStepCapturedListener>()

        /**
         * Fecha qualquer popup, diálogo ou menu ativo antes de iniciar a transferência
         */
        fun fecharPopupsAtivos(): Boolean {
            val instance = activeInstance ?: return false
            val rootNode = instance.rootInActiveWindow ?: return false
            return instance.findAndClickDismissButton(rootNode)
        }

        fun addStepListener(listener: OnStepCapturedListener) {
            listeners.add(listener)
        }

        fun removeStepListener(listener: OnStepCapturedListener) {
            listeners.remove(listener)
        }

        /**
         * Inicializa o fluxo de automação passo a passo do código *162#
         * Fecha qualquer popup/menu ativo na tela antes de executar
         */
        fun iniciarFluxoPreciso(megas: String, numero: String) {
            fecharPopupsAtivos()
            targetMegas.value = megas
            targetNumero.value = numero
            _stepCapturesHistory.value = emptyList()
            _ultimaRespostaFinal.value = ""
            _currentActiveStep.value = Step.WAITING_STEP_8
            _stepLogFlow.value = "Disparando *162# - Aguardando menu da operadora..."
        }

        /**
         * Cancela a automação em andamento
         */
        fun cancelarFluxo() {
            _currentActiveStep.value = Step.IDLE
            _stepLogFlow.value = "Fluxo USSD cancelado."
        }

        /**
         * Retorna a lista de todas as capturas de tela e respostas gravadas no fluxo
         */
        fun getRespostasPorEtapa(): List<StepCapture> {
            return _stepCapturesHistory.value
        }

        /**
         * Retorna a captura de uma etapa específica
         */
        fun getRespostaEtapa(step: Step): StepCapture? {
            return _stepCapturesHistory.value.firstOrNull { it.step == step }
        }

        /**
         * Retorna a resposta final capturada da operadora
         */
        fun getUltimaRespostaOperadora(): String {
            return _ultimaRespostaFinal.value.ifBlank {
                _stepCapturesHistory.value.lastOrNull()?.screenContent ?: ""
            }
        }
    }
}
