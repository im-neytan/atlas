package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.Bl4ckOnPrimary
import com.example.ui.theme.Bl4ckPrimary
import com.example.ui.theme.Bl4ckPrimaryGlow
import com.example.ui.theme.Bl4ckSecondary
import com.example.ui.theme.Bl4ckSurface
import com.example.ui.theme.Bl4ckSurfaceElevated
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Card: Fintech Mission Control & Live Status
        item {
            ExecutiveWelcomeHeader(
                connectionStatus = connectionStatus,
                isEngineRunning = isEngineRunning,
                currentCountdown = currentCountdown,
                onAlternarMotor = handleAlternarMotor
            )
        }

        // 2. Operações & Balanço: Grid 2x2 de Cartões de Métricas
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
                    badge = if (aguardandoCount > 0) "EM FILA" else "VAZIO",
                    modifier = Modifier.weight(1f),
                    onClick = onVerFilaClick
                )

                MetricSummaryCard(
                    title = "CONCLUÍDOS",
                    value = "$concluidosCount",
                    subValue = if (falhasCount > 0) "$falhasCount falhas" else "100% sucesso",
                    accentColor = Bl4ckPrimary,
                    badge = "HISTÓRICO",
                    modifier = Modifier.weight(1f),
                    onClick = onVerHistoricoClick
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
                    value = if (!hasSims) "Sem cartões" else "SIM $activeSim",
                    subValue = if (!hasSims) "Nenhum chip inserido" else "${activeSimInfo.carrierName} • ${activeSimInfo.remainingSends}/${activeSimInfo.totalLimit}",
                    accentColor = if (!hasSims) Bl4ckTextMuted else if (activeSimInfo.isLimitReached) Bl4ckError else Bl4ckPrimary,
                    badge = activeSimInfo.carrierName.take(8),
                    modifier = Modifier.weight(1f),
                    onClick = onAlternarSim
                )

                MetricSummaryCard(
                    title = "MOTOR USSD",
                    value = if (isEngineRunning) "ATIVO" else "PAUSADO",
                    subValue = if (isEngineRunning && currentCountdown > 0) "${currentCountdown}s próx." else "Ciclo 4-8s",
                    accentColor = if (isEngineRunning) Bl4ckPrimary else Bl4ckWarning,
                    badge = if (isEngineRunning) "ONLINE" else "PAUSA",
                    modifier = Modifier.weight(1f),
                    onClick = handleAlternarMotor
                )
            }
        }

        // 3. Central de Controle Operacional
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

        // 4. Cartão de Gestão do SIM Ativo com Barra de Quota
        item {
            ActiveSimQuotaCard(
                activeSim = activeSim,
                activeSimInfo = activeSimInfo,
                onAlternarSim = onAlternarSim
            )
        }

        // 5. Última Execução e Resposta da Operadora (se houver)
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
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Bl4ckSurface,
                        contentColor = Bl4ckTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Bl4ckBorder),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Ver Fila Completa", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("$totalFila pedidos ativos", fontSize = 11.sp, color = Bl4ckTextSecondary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Bl4ckTextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                OutlinedButton(
                    onClick = onAlternarSim,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Bl4ckSurface,
                        contentColor = Bl4ckTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Bl4ckBorder),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Alternar SIM", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Agora: SIM $activeSim", fontSize = 11.sp, color = Bl4ckPrimary)
                        }
                        Icon(Icons.Default.SimCard, contentDescription = null, tint = Bl4ckPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/**
 * Header Executivo Fintech / Mission Control:
 * Mostra status em tempo real, status do WebSocket e controle rápido.
 */
@Composable
private fun ExecutiveWelcomeHeader(
    connectionStatus: ConnectionStatus,
    isEngineRunning: Boolean,
    currentCountdown: Int,
    onAlternarMotor: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radarPulse"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                colors = listOf(Bl4ckBorder, Bl4ckBorderSubtle)
            ),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Bl4ckSurfaceVariant.copy(alpha = 0.5f), Bl4ckSurface)
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status de Operação do Motor
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isEngineRunning) Bl4ckPrimary.copy(alpha = 0.12f) else Bl4ckSurfaceElevated)
                        .border(
                            1.dp,
                            if (isEngineRunning) Bl4ckPrimary.copy(alpha = 0.3f) else Bl4ckBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .alpha(if (isEngineRunning) radarPulse else 0.5f)
                                .clip(CircleShape)
                                .background(if (isEngineRunning) Bl4ckPrimary else Bl4ckWarning)
                        )
                        Text(
                            text = if (isEngineRunning) "MOTOR ATIVO" else "MOTOR PAUSADO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = if (isEngineRunning) Bl4ckPrimary else Bl4ckWarning
                        )
                    }
                }

                // Status da Conexão WebSocket
                val (connColor, connLabel) = when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> Bl4ckPrimary to "WEBSOCKET ONLINE"
                    ConnectionStatus.CONNECTING -> Bl4ckWarning to "CONECTANDO..."
                    ConnectionStatus.DISCONNECTED -> Bl4ckTextMuted to "LOCAL STANDBY"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(connColor.copy(alpha = 0.1f))
                        .border(1.dp, connColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
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
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = connColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "CENTRAL DE CONTROLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Bl4ckTextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Relaxa, Bl4ck System a operar.",
                        style = MaterialTheme.typography.titleLarge,
                        color = Bl4ckTextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }
            }
        }
    }
}

/**
 * Card de Métrica Individual com visual Fintech
 */
@Composable
private fun MetricSummaryCard(
    title: String,
    value: String,
    subValue: String,
    accentColor: Color,
    badge: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
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
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Bl4ckTextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )

                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subValue,
                fontSize = 11.sp,
                color = Bl4ckTextSecondary,
                maxLines = 1
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isEngineRunning) Bl4ckPrimary.copy(alpha = 0.35f) else Bl4ckBorder
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
                        text = "Ações Operacionais Rápidas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Bl4ckTextPrimary
                    )
                    Text(
                        text = if (isEngineRunning) {
                            if (currentCountdown > 0) "Próximo envio em ${currentCountdown}s (automático)" else "Processando fila em segundo plano"
                        } else {
                            "Motor em pausa. Intervalo configurado: 4 a 8s"
                        },
                        fontSize = 12.sp,
                        color = if (isEngineRunning) Bl4ckPrimary else Bl4ckTextSecondary
                    )
                }

                if (isEngineRunning) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Bl4ckPrimary.copy(alpha = 0.12f))
                            .border(1.dp, Bl4ckPrimary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AUTO 4-8s",
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAlternarMotor,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEngineRunning) Bl4ckSurfaceElevated else Bl4ckPrimary,
                        contentColor = if (isEngineRunning) Bl4ckError else Bl4ckOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.3f).testTag("btn_toggle_engine")
                ) {
                    Icon(
                        imageVector = if (isEngineRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isEngineRunning) "Pausar Motor" else "Iniciar Motor",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                Button(
                    onClick = onAgendarClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Bl4ckSurfaceVariant,
                        contentColor = Bl4ckTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
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
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Bl4ckSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Bl4ckSecondary.copy(alpha = 0.4f)),
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
 * Card dedicado de Quota e Status do SIM ativo
 */
@Composable
private fun ActiveSimQuotaCard(
    activeSim: Int,
    activeSimInfo: SimInfo,
    onAlternarSim: () -> Unit
) {
    val totalLimit = if (activeSimInfo.totalLimit > 0) activeSimInfo.totalLimit else 100
    val usedSends = (totalLimit - activeSimInfo.remainingSends).coerceAtLeast(0)
    val progress = (usedSends.toFloat() / totalLimit.toFloat()).coerceIn(0f, 1f)

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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Bl4ckSurfaceVariant)
                            .border(1.dp, Bl4ckBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SimCard,
                            contentDescription = null,
                            tint = Bl4ckPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "SIM $activeSim • ${activeSimInfo.carrierName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Bl4ckTextPrimary
                        )
                        Text(
                            text = "Chip selecionado para chamadas USSD",
                            fontSize = 11.sp,
                            color = Bl4ckTextSecondary
                        )
                    }
                }

                Button(
                    onClick = onAlternarSim,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Bl4ckSurfaceElevated,
                        contentColor = Bl4ckPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Trocar SIM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Barra de Progresso da Quota do Dia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quota Diária de Envios",
                    fontSize = 11.sp,
                    color = Bl4ckTextMuted,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${activeSimInfo.remainingSends} restantes de $totalLimit",
                    fontSize = 11.sp,
                    color = if (activeSimInfo.isLimitReached) Bl4ckError else Bl4ckTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (activeSimInfo.isLimitReached) Bl4ckError else Bl4ckPrimary,
                trackColor = Bl4ckSurfaceVariant
            )
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
            brush = androidx.compose.ui.graphics.SolidColor(Bl4ckBorder),
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
                        color = Bl4ckTextPrimary,
                        fontFamily = FontFamily.Monospace
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
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0A0F17))
                        .border(1.dp, Bl4ckBorderSubtle, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "RESPOSTA DA OPERADORA:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Bl4ckTextMuted,
                            letterSpacing = 0.5.sp
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
