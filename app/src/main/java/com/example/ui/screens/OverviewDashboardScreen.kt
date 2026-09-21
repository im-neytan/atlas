package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoricoItem
import com.example.data.model.SimInfo
import com.example.network.ConnectionStatus
import com.example.ui.theme.Bl4ckBackground
import com.example.ui.theme.Bl4ckBorder
import com.example.ui.theme.Bl4ckBorderSubtle
import com.example.ui.theme.Bl4ckError
import com.example.ui.theme.Bl4ckPrimary
import com.example.ui.theme.Bl4ckSecondary
import com.example.ui.theme.Bl4ckSurface
import com.example.ui.theme.Bl4ckSurfaceVariant
import com.example.ui.theme.Bl4ckTextMuted
import com.example.ui.theme.Bl4ckTextPrimary
import com.example.ui.theme.Bl4ckTextSecondary
import com.example.ui.theme.Bl4ckWarning
import com.example.util.SystemServicesHelper

@Composable
fun OverviewDashboardScreen(
    totalFila: Int,
    aguardandoCount: Int,
    emProcessamentoCount: Int,
    concluidosCount: Int,
    falhasCount: Int,
    activeSim: Int,
    sim1Info: SimInfo,
    sim2Info: SimInfo,
    isEngineRunning: Boolean,
    currentCountdown: Int,
    connectionStatus: ConnectionStatus,
    ultimoHistorico: HistoricoItem?,
    onAgendarClick: () -> Unit,
    onAlternarMotor: () -> Unit,
    onProcessarProximo: () -> Unit,
    onVerFilaClick: () -> Unit,
    onVerHistoricoClick: () -> Unit,
    onAlternarSim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isAccessibilityActive by remember { mutableStateOf(false) }
    var isCallGranted by remember { mutableStateOf(false) }
    var isOverlayGranted by remember { mutableStateOf(false) }

    fun refreshServicesState() {
        isAccessibilityActive = SystemServicesHelper.isAccessibilityServiceEnabled(context)
        isCallGranted = SystemServicesHelper.isCallPermissionGranted(context)
        isOverlayGranted = SystemServicesHelper.canDrawOverlays(context)
    }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isCallGranted = granted
        refreshServicesState()
    }

    LaunchedEffect(Unit) {
        refreshServicesState()
        if (!SystemServicesHelper.isCallPermissionGranted(context)) {
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    val handleAlternarMotor = {
        if (!isCallGranted) {
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
        onAlternarMotor()
    }

    val handleProcessarProximo = {
        if (!isCallGranted) {
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
        onProcessarProximo()
    }

    val activeSimInfo = if (activeSim == 1) sim1Info else sim2Info

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Bl4ckBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Inicial Executivo: Bem-vindo, Relaxa Bl4ck System a operar
        item {
            ExecutiveWelcomeHeader(
                connectionStatus = connectionStatus,
                isEngineRunning = isEngineRunning
            )
        }

        // 2. Grid de Métricas Principais (Fila, Concluídos, SIM, Motor)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSummaryCard(
                    title = "FILA ATIVA",
                    value = "$aguardandoCount",
                    subValue = if (emProcessamentoCount > 0) "1 em processo" else "Pendentes",
                    accentColor = if (aguardandoCount > 0) Bl4ckSecondary else Bl4ckTextMuted,
                    modifier = Modifier.weight(1f)
                )

                MetricSummaryCard(
                    title = "CONCLUÍDOS",
                    value = "$concluidosCount",
                    subValue = if (falhasCount > 0) "$falhasCount falhas" else "100% sucesso",
                    accentColor = Bl4ckPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val hasSims = sim1Info.isInserted || sim2Info.isInserted
                MetricSummaryCard(
                    title = "CHIP DE CHAMADA",
                    value = if (!hasSims) "Sem cartões ativos" else activeSimInfo.carrierName,
                    subValue = if (!hasSims) "Nenhum chip inserido" else "SIM $activeSim • ${activeSimInfo.remainingSends}/${activeSimInfo.totalLimit} envios",
                    accentColor = if (!hasSims) Bl4ckTextMuted else if (activeSimInfo.isLimitReached) Bl4ckError else Bl4ckPrimary,
                    modifier = Modifier.weight(1f)
                )

                MetricSummaryCard(
                    title = "MOTOR USSD",
                    value = if (isEngineRunning) "ATIVO" else "PAUSADO",
                    subValue = if (isEngineRunning && currentCountdown > 0) "${currentCountdown}s próx." else "Ciclo 4-8s",
                    accentColor = if (isEngineRunning) Bl4ckPrimary else Bl4ckWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Controle Operacional Principal
        item {
            OperationalControlCard(
                isEngineRunning = isEngineRunning,
                currentCountdown = currentCountdown,
                hasPending = aguardandoCount > 0,
                onAlternarMotor = handleAlternarMotor,
                onAgendarClick = onAgendarClick,
                onProcessarProximo = handleProcessarProximo
            )
        }

        // 4. Última Execução e Resposta da Operadora (se houver)
        if (ultimoHistorico != null) {
            item {
                LatestExecutionCard(
                    item = ultimoHistorico,
                    onVerTodos = onVerHistoricoClick
                )
            }
        }

        // 6. Atalhos Rápidos para Abas
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onVerFilaClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Bl4ckSurface,
                        contentColor = Bl4ckTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Bl4ckBorderSubtle),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Ver Fila", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("$totalFila pedidos", fontSize = 11.sp, color = Bl4ckTextSecondary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Bl4ckTextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                OutlinedButton(
                    onClick = onAlternarSim,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Bl4ckSurface,
                        contentColor = Bl4ckTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Bl4ckBorderSubtle),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Alternar SIM", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Agora: SIM $activeSim", fontSize = 11.sp, color = Bl4ckTextSecondary)
                        }
                        Icon(Icons.Default.SimCard, contentDescription = null, tint = Bl4ckTextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/**
 * Header Executivo Suave:
 * "Bem-vindo"
 * "Relaxa, Bl4ck System a operar"
 */
@Composable
private fun ExecutiveWelcomeHeader(
    connectionStatus: ConnectionStatus,
    isEngineRunning: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Bl4ckBorder),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status do Sistema
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Bl4ckSurfaceVariant)
                        .border(1.dp, Bl4ckBorderSubtle, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Bl4ckPrimary)
                        )
                        Text(
                            text = "Sistema Automático",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Bl4ckTextSecondary
                        )
                    }
                }

                // Status da Conexão
                val (connColor, connLabel) = when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> Bl4ckPrimary to "Online"
                    ConnectionStatus.CONNECTING -> Bl4ckWarning to "Conectando"
                    ConnectionStatus.DISCONNECTED -> Bl4ckTextMuted to "Pronto"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(connColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(connColor)
                        )
                        Text(
                            text = connLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = connColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Bem-vindo",
                style = MaterialTheme.typography.labelMedium,
                color = Bl4ckTextSecondary,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Relaxa, Bl4ck System a operar.",
                style = MaterialTheme.typography.headlineSmall,
                color = Bl4ckTextPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

/**
 * Card de Métrica Individual com visual corporativo
 */
@Composable
private fun MetricSummaryCard(
    title: String,
    value: String,
    subValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Bl4ckBorderSubtle),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Bl4ckTextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subValue,
                fontSize = 11.sp,
                color = Bl4ckTextSecondary
            )
        }
    }
}

/**
 * Painel Operacional: Motor USSD & Agendamento
 */
@Composable
private fun OperationalControlCard(
    isEngineRunning: Boolean,
    currentCountdown: Int,
    hasPending: Boolean,
    onAlternarMotor: () -> Unit,
    onAgendarClick: () -> Unit,
    onProcessarProximo: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isEngineRunning) Bl4ckPrimary.copy(alpha = 0.4f) else Bl4ckBorder
            ),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Controle de Execução",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Bl4ckTextPrimary
                    )
                    Text(
                        text = if (isEngineRunning) {
                            if (currentCountdown > 0) "Próximo envio em ${currentCountdown}s" else "Executando pedidos na fila"
                        } else {
                            "Motor em espera. Intervalo configurado: 4 a 8s"
                        },
                        fontSize = 12.sp,
                        color = if (isEngineRunning) Bl4ckPrimary else Bl4ckTextSecondary
                    )
                }

                if (isEngineRunning) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Bl4ckPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AUTOMÁTICO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Bl4ckPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAlternarMotor,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEngineRunning) Bl4ckSurfaceVariant else Bl4ckPrimary,
                        contentColor = if (isEngineRunning) Bl4ckError else Bl4ckBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.3f).testTag("btn_toggle_engine")
                ) {
                    Icon(
                        imageVector = if (isEngineRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isEngineRunning) "Pausar" else "Iniciar Motor",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Button(
                    onClick = onAgendarClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Bl4ckSurfaceVariant,
                        contentColor = Bl4ckTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.3f).testTag("btn_new_transfer")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Bl4ckPrimary)
                    Text(
                        text = "Nova Transferência",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                if (!isEngineRunning && hasPending) {
                    OutlinedButton(
                        onClick = onProcessarProximo,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Bl4ckSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Bl4ckBorderSubtle),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Text("Disparar 1", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Painel de Serviços do Sistema e Permissões
 * (Leitura de Tela/Acessibilidade, Chamadas, Sobreposição)
 */
@Composable
private fun SystemServicesCard(
    isAccessibilityActive: Boolean,
    isCallGranted: Boolean,
    isOverlayGranted: Boolean,
    onRequestCall: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenOverlay: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Bl4ckBorderSubtle),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Serviços & Permissões do Sistema",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Bl4ckTextPrimary
                )

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Atualizar Status",
                        tint = Bl4ckTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Serviço de Acessibilidade (Automação precisa *162# e captura de resposta)
            ServiceStatusRow(
                title = "Automação & Leitura (Acessibilidade)",
                subtitle = "Interação em *162# e captura da resposta final",
                isOk = isAccessibilityActive,
                actionLabel = if (isAccessibilityActive) "Ativo" else "Ativar",
                onClickAction = onOpenAccessibility
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Chamadas Telefônicas (Disparo do código *162#)
            ServiceStatusRow(
                title = "Chamadas Telefônicas",
                subtitle = "Disparo inicial do código *162#",
                isOk = isCallGranted,
                actionLabel = if (isCallGranted) "Concedido" else "Autorizar",
                onClickAction = onRequestCall
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Sobreposição de Telas
            ServiceStatusRow(
                title = "Sobreposição a Outros Apps",
                subtitle = "Interação em segundo plano",
                isOk = isOverlayGranted,
                actionLabel = if (isOverlayGranted) "Habilitado" else "Habilitar",
                onClickAction = onOpenOverlay
            )
        }
    }
}

/**
 * Cartão exibindo o roteiro do fluxo preciso interativo *162#
 */
@Composable
fun UssdSequenceFlowCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Bl4ckBorderSubtle),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FLUXO PRECISO DE EXECUÇÃO (*162#)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Bl4ckSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Bl4ckPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "MODO USSD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Bl4ckPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val passos = listOf(
                "1" to "*162#",
                "2" to "Espera ➔ Opção '8'",
                "3" to "Espera ➔ Opção '2'",
                "4" to "Espera ➔ Digita Megas",
                "5" to "Espera ➔ Digita Número",
                "✓" to "Captura Resposta Final"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                passos.take(3).forEach { (num, desc) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Bl4ckSurfaceVariant)
                            .border(1.dp, Bl4ckBorderSubtle, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(text = "Passo $num", fontSize = 9.sp, color = Bl4ckTextMuted, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = desc, fontSize = 10.sp, color = Bl4ckTextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                passos.drop(3).forEach { (num, desc) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (num == "✓") Bl4ckPrimary.copy(alpha = 0.12f) else Bl4ckSurfaceVariant)
                            .border(1.dp, if (num == "✓") Bl4ckPrimary.copy(alpha = 0.4f) else Bl4ckBorderSubtle, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = if (num == "✓") "Conclusão" else "Passo $num",
                                fontSize = 9.sp,
                                color = if (num == "✓") Bl4ckPrimary else Bl4ckTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                fontSize = 10.sp,
                                color = if (num == "✓") Bl4ckPrimary else Bl4ckTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceStatusRow(
    title: String,
    subtitle: String,
    isOk: Boolean,
    actionLabel: String,
    onClickAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Bl4ckSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Bl4ckTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Bl4ckTextMuted
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isOk) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Bl4ckPrimary.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "✓ $actionLabel",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Bl4ckPrimary
                )
            }
        } else {
            Button(
                onClick = onClickAction,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Bl4ckWarning,
                    contentColor = Bl4ckBackground
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Card de Última Execução e Resposta da Operadora
 */
@Composable
private fun LatestExecutionCard(
    item: HistoricoItem,
    onVerTodos: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Bl4ckBorderSubtle),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Última Operação Concluída",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Bl4ckTextPrimary
                )

                Text(
                    text = item.formattedTime,
                    fontSize = 11.sp,
                    color = Bl4ckTextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.numeroDestino,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Bl4ckTextPrimary
                    )
                    Text(
                        text = "${item.megas} MB • SIM ${item.simSlot}",
                        fontSize = 11.sp,
                        color = Bl4ckTextSecondary
                    )
                }

                StatusBadge(status = item.status)
            }

            if (item.ussdResposta.isNotBlank()) {
                val limpo = item.ussdResposta.trim()
                    .replace(Regex("(?i)\\b(ok|fechar|close|cancelar|send|enviar|dismiss|entendido)\\b"), "")
                    .replace(Regex("\\s+"), " ").trim()

                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D1420))
                        .border(1.dp, Bl4ckBorderSubtle, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "RESPOSTA DA OPERADORA:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Bl4ckTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = limpo.ifBlank { item.ussdResposta },
                            fontSize = 11.sp,
                            color = Bl4ckSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
