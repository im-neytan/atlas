package com.example.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
 * Serviço de Acessibilidade para automação precisa passo a passo do fluxo USSD *162#:
 *  1. *162# -> Aguarda resposta do menu principal -> Seleciona opção 8 -> Envia
 *  2. Aguarda resposta do submenu -> Seleciona opção 2 -> Envia
 *  3. Aguarda tela de megas -> Insere quantidade de megas -> Envia
 *  4. Aguarda tela de número -> Insere número do destinatário -> Envia
 *  5. Aguarda popup de resposta final -> Lê exclusivamente o texto da mensagem com precisão -> Salva -> Clica OK para fechar
 */
class UssdAccessibilityService : AccessibilityService() {

    enum class Step(val label: String) {
        IDLE("Inativo"),
        WAITING_STEP_8("1. Aguardando Menu Principal (*162#)"),
        WAITING_STEP_2("2. Aguardando Submenu (Opção 2)"),
        WAITING_STEP_MEGAS("3. Aguardando Campo de Megas"),
        WAITING_STEP_NUMERO("4. Aguardando Campo de Número"),
        WAITING_FINAL_RESPONSE("5. Aguardando Resposta Final")
    }

    interface OnStepCapturedListener {
        fun onStepCaptured(capture: StepCapture)
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    // Trava de sincronização estrita para evitar reentrâncias ou disparos prematuros
    @Volatile
    private var isProcessingStep = false

    // Registra o texto manipulado na etapa anterior para detectar alteração real da tela
    private var lastHandledText: String = ""

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        try {
            val eventType = event.eventType
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            ) {
                val rootNode = rootInActiveWindow ?: event.source ?: return
                val currentStep = _currentActiveStep.value

                // Se inativo, apenas registra diálogos soltos no modo passivo
                if (currentStep == Step.IDLE) {
                    val capturedText = extractTextFromNode(rootNode)
                    if (capturedText.isNotBlank() && isUssdDialog(rootNode, capturedText)) {
                        _lastCapturedScreenText.value = capturedText
                        ServidorManager.getInstance(applicationContext).registrarRespostaUssd(capturedText)
                    }
                    return
                }

                // Se já estiver processando ativamente uma ação deste passo, aguarda
                if (isProcessingStep) return

                val capturedText = extractTextFromNode(rootNode)
                if (capturedText.isBlank()) return

                _lastCapturedScreenText.value = capturedText

                // Executa a verificação e avanço da etapa correspondente
                handleAutomatedStep(rootNode, capturedText, currentStep)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro no evento de acessibilidade: ${e.message}")
        }
    }

    /**
     * Gerencia a execução estrita e ordenada do fluxo passo a passo:
     * *162# -> 8 -> 2 -> Megas -> Número -> Ler Resposta Final -> OK Fechar
     */
    private fun handleAutomatedStep(
        rootNode: AccessibilityNodeInfo,
        capturedText: String,
        currentStep: Step
    ) {
        val inputNode = findInputEditText(rootNode)
        val sendButton = findSendButton(rootNode)

        when (currentStep) {
            // =========================================================================
            // ETAPA 1: *162# -> Esperar resposta -> Selecionar 8
            // =========================================================================
            Step.WAITING_STEP_8 -> {
                if (inputNode != null) {
                    isProcessingStep = true
                    lastHandledText = capturedText
                    recordStepCapture(Step.WAITING_STEP_8, 1, capturedText, "8")
                    _stepLogFlow.value = "1/5: Menu *162# detectado. Aguardando estabilização e digitando 8..."

                    serviceScope.launch {
                        delay(700) // Aguarda a janela USSD estabilizar completamente
                        val currentRoot = rootInActiveWindow ?: rootNode
                        val freshInput = findInputEditText(currentRoot) ?: inputNode
                        val freshSend = findSendButton(currentRoot) ?: sendButton

                        sendInputText(freshInput, "8", freshSend)
                        _stepLogFlow.value = "1/5: Opção 8 enviada. Aguardando resposta do submenu..."
                        delay(900) // Aguarda a rede começar a transição da tela
                        _currentActiveStep.value = Step.WAITING_STEP_2
                        isProcessingStep = false
                    }
                }
            }

            // =========================================================================
            // ETAPA 2: Esperar resposta da opção 8 -> Selecionar 2
            // =========================================================================
            Step.WAITING_STEP_2 -> {
                // Certifica-se de que a tela mudou do menu principal anterior
                if (capturedText == lastHandledText) return

                if (inputNode != null) {
                    isProcessingStep = true
                    lastHandledText = capturedText
                    recordStepCapture(Step.WAITING_STEP_2, 2, capturedText, "2")
                    _stepLogFlow.value = "2/5: Submenu recebido. Aguardando estabilização e digitando 2..."

                    serviceScope.launch {
                        delay(700) // Aguarda resposta estabilizar
                        val currentRoot = rootInActiveWindow ?: rootNode
                        val freshInput = findInputEditText(currentRoot) ?: inputNode
                        val freshSend = findSendButton(currentRoot) ?: sendButton

                        sendInputText(freshInput, "2", freshSend)
                        _stepLogFlow.value = "2/5: Opção 2 enviada. Aguardando campo de Megas..."
                        delay(900)
                        _currentActiveStep.value = Step.WAITING_STEP_MEGAS
                        isProcessingStep = false
                    }
                }
            }

            // =========================================================================
            // ETAPA 3: Esperar tela de megas -> Colocar Megas
            // =========================================================================
            Step.WAITING_STEP_MEGAS -> {
                // Certifica-se de que a tela mudou do submenu anterior
                if (capturedText == lastHandledText) return

                if (inputNode != null) {
                    val megas = targetMegas.value
                    isProcessingStep = true
                    lastHandledText = capturedText
                    recordStepCapture(Step.WAITING_STEP_MEGAS, 3, capturedText, megas)
                    _stepLogFlow.value = "3/5: Campo de Megas recebido. Inserindo $megas MB..."

                    serviceScope.launch {
                        delay(700) // Aguarda resposta estabilizar
                        val currentRoot = rootInActiveWindow ?: rootNode
                        val freshInput = findInputEditText(currentRoot) ?: inputNode
                        val freshSend = findSendButton(currentRoot) ?: sendButton

                        sendInputText(freshInput, megas, freshSend)
                        _stepLogFlow.value = "3/5: Megas ($megas) enviados. Aguardando campo do número destinatário..."
                        delay(900)
                        _currentActiveStep.value = Step.WAITING_STEP_NUMERO
                        isProcessingStep = false
                    }
                }
            }

            // =========================================================================
            // ETAPA 4: Esperar tela de número -> Colocar Número
            // =========================================================================
            Step.WAITING_STEP_NUMERO -> {
                // Certifica-se de que a tela mudou da tela de megas anterior
                if (capturedText == lastHandledText) return

                if (inputNode != null) {
                    val numero = targetNumero.value
                    isProcessingStep = true
                    lastHandledText = capturedText
                    recordStepCapture(Step.WAITING_STEP_NUMERO, 4, capturedText, numero)
                    _stepLogFlow.value = "4/5: Campo de Destinatário recebido. Inserindo $numero..."

                    serviceScope.launch {
                        delay(700) // Aguarda resposta estabilizar
                        val currentRoot = rootInActiveWindow ?: rootNode
                        val freshInput = findInputEditText(currentRoot) ?: inputNode
                        val freshSend = findSendButton(currentRoot) ?: sendButton

                        sendInputText(freshInput, numero, freshSend)
                        _stepLogFlow.value = "4/5: Número ($numero) enviado. Aguardando popup de resposta final..."
                        delay(1000) // Aguarda o envio do número ser submetido
                        _currentActiveStep.value = Step.WAITING_FINAL_RESPONSE
                        isProcessingStep = false
                    }
                }
            }

            // =========================================================================
            // ETAPA 5: Esperar, Ler a resposta final com precisão e depois OK Fechar
            // =========================================================================
            Step.WAITING_FINAL_RESPONSE -> {
                // Ignora se a tela ainda for idêntica ao prompt do número anterior
                if (capturedText == lastHandledText) return

                // Se ainda for um campo de edição com prompt de destinatário, aguarda a resposta real
                val lower = capturedText.lowercase()
                if (lower.contains("destinatario") || lower.contains("numero do") || lower.contains("número")) {
                    if (inputNode != null) return
                }

                // Extrai com máxima precisão somente o texto do corpo da mensagem dentro do popup
                val preciseText = extractPreciseDialogText(rootNode)
                if (preciseText.isBlank() || preciseText.equals("ok", ignoreCase = true) || preciseText.length < 5) {
                    return
                }

                isProcessingStep = true
                lastHandledText = capturedText
                _stepLogFlow.value = "5/5: Popup final detectado. Aguardando estabilização para leitura precisa..."

                serviceScope.launch {
                    delay(800) // Espera para ler claramente e com total estabilidade visual
                    val currentRoot = rootInActiveWindow ?: rootNode
                    val finalResponseText = extractPreciseDialogText(currentRoot).ifBlank { preciseText }

                    // Grava o texto capturado com precisão
                    recordStepCapture(Step.WAITING_FINAL_RESPONSE, 5, finalResponseText, "[FIM]")
                    _ultimaRespostaFinal.value = finalResponseText
                    _stepLogFlow.value = "Resposta final lida com precisão: \"$finalResponseText\""

                    // Registra no ServidorManager
                    ServidorManager.getInstance(applicationContext).registrarRespostaUssd(finalResponseText)

                    // Espera clara após ler a resposta antes de acionar o botão OK/Fechar
                    delay(1200)

                    _stepLogFlow.value = "Fechando diálogo final com OK..."
                    val dismissed = findAndClickDismissButton(rootInActiveWindow ?: currentRoot)
                    if (!dismissed) {
                        delay(400)
                        findAndClickDismissButton(rootInActiveWindow ?: currentRoot)
                    }

                    _stepLogFlow.value = "Fluxo USSD concluído com sucesso."
                    _currentActiveStep.value = Step.IDLE
                    isProcessingStep = false
                }
            }

            Step.IDLE -> {
                // Inativo
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
     */
    private fun sendInputText(
        inputNode: AccessibilityNodeInfo,
        text: String,
        sendButton: AccessibilityNodeInfo?
    ) {
        try {
            if (!inputNode.isFocused) {
                inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            }

            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            val textSet = inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

            if (!textSet) {
                inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            }

            if (sendButton != null) {
                val clicked = clickNodeOrParent(sendButton)
                if (!clicked) {
                    sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            } else {
                inputNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar texto: ${e.message}")
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
        val className = node.className?.toString() ?: ""
        if (className == EditText::class.java.name ||
            className.contains("EditText", ignoreCase = true) ||
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
        val sendLabels = listOf("enviar", "send", "submeter", "responder", "reply", "continuar", "ok")
        val nodeText = node.text?.toString()?.trim()?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        if (viewId.endsWith("button1") || viewId.contains("button_positive") || viewId.contains("send")) {
            return node
        }

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
     * Localiza e clica no botão OK/Fechar do último popup
     */
    private fun findAndClickDismissButton(node: AccessibilityNodeInfo): Boolean {
        val dismissLabels = listOf("ok", "fechar", "close", "entendido", "dismiss", "cancelar", "aceitar", "concluido", "concluído")
        val nodeText = node.text?.toString()?.trim()?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        if (viewId.endsWith("button1") || viewId.contains("button_positive")) {
            if (clickNodeOrParent(node)) return true
        }

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
     * Extrai com máxima precisão somente o texto do corpo da mensagem dentro do popup final,
     * ignorando botões ("OK", "Fechar", "Cancelar") e campos vazios.
     */
    private fun extractPreciseDialogText(rootNode: AccessibilityNodeInfo): String {
        // 1. Tenta encontrar diretamente o TextView padrão de mensagem do diálogo Android (android:id/message)
        val messageNodes = rootNode.findAccessibilityNodeInfosByViewId("android:id/message")
        if (!messageNodes.isNullOrEmpty()) {
            val msg = messageNodes[0].text?.toString()?.trim()
            if (!msg.isNullOrBlank()) {
                return msg
            }
        }

        // 2. Coleta textos de TextViews excluindo botões e cabeçalhos genéricos
        val collected = mutableListOf<String>()
        val excludedButtons = setOf(
            "ok", "fechar", "close", "cancelar", "cancel", "dismiss",
            "entendido", "enviar", "send", "resposta", "responder",
            "concluido", "concluído", "aceitar"
        )

        fun traverse(node: AccessibilityNodeInfo) {
            val className = node.className?.toString() ?: ""
            val rawText = node.text?.toString()?.trim() ?: ""
            val isButton = node.isClickable ||
                    className.contains("Button", ignoreCase = true) ||
                    excludedButtons.contains(rawText.lowercase())

            // Ignora caixas de texto editáveis
            if (className.contains("EditText", ignoreCase = true)) {
                return
            }

            if (rawText.isNotBlank() && !isButton) {
                if (!excludedButtons.contains(rawText.lowercase())) {
                    if (!collected.contains(rawText)) {
                        collected.add(rawText)
                    }
                }
            }

            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                traverse(child)
            }
        }

        traverse(rootNode)

        // Se coletou textos válidos, une e retorna
        val filtered = collected.filter { it.length > 1 && !it.equals("USSD", ignoreCase = true) }
        val full = filtered.joinToString(" ").trim()
        return full.ifBlank { extractTextFromNode(rootNode) }
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
        isProcessingStep = false
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
         * Fecha qualquer popup ou menu que tenha ficado aberto de execuções anteriores,
         * somente se o serviço estiver inativo (IDLE).
         */
        fun fecharPopupsAtivos(): Boolean {
            if (_currentActiveStep.value != Step.IDLE) return false
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
         */
        fun iniciarFluxoPreciso(megas: String, numero: String) {
            fecharPopupsAtivos()
            targetMegas.value = megas
            targetNumero.value = numero
            _stepCapturesHistory.value = emptyList()
            _ultimaRespostaFinal.value = ""
            _currentActiveStep.value = Step.WAITING_STEP_8
            _stepLogFlow.value = "Disparando *162# - Aguardando menu principal..."
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
