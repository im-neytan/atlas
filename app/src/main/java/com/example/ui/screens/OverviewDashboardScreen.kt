package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoricoItem
import com.example.data.model.SimInfo
import com.example.network.ConnectionStatus
import com.example.ui.theme.Bl4ckBackground
import com.example.ui.theme.Bl4ckBorder
import com.example.ui.theme.Bl4ckBorderSubtle
import com.example.ui.theme.Bl4ckError
import com.example.ui.theme.Bl4ckGlassBorder
import com.example.ui.theme.Bl4ckGlassBorderSubtle
import com.example.ui.theme.Bl4ckGlassHighlight
import com.example.ui.theme.Bl4ckGlassSurface
import com.example.ui.theme.Bl4ckGlassSurfaceLight
import com.example.ui.theme.Bl4ckOnPrimary
import com.example.ui.theme.Bl4ckPillGreen
import com.example.ui.theme.Bl4ckPillGreenBg
import com.example.ui.theme.Bl4ckPillGreenBorder
import com.example.ui.theme.Bl4ckPrimary
import com.example.ui.theme.Bl4ckSecondary
import com.example.ui.theme.Bl4ckSphereInnerOrange
import com.example.ui.theme.Bl4ckSphereInnerOrangeDark
import com.example.ui.theme.Bl4ckSphereOuterGlow
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

    var isCallGranted by remember { mutableStateOf(false) }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isCallGranted = granted
    }

    LaunchedEffect(Unit) {
        isCallGranted = SystemServicesHelper.isCallPermissionGranted(context)
        if (!isCallGranted) {
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
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Hero Section: Ambient Liquid Glass Sphere & Mission Control (Inspirado na imagem de referência)
        item {
            HeroLiquidGlassSphere(
                aguardandoCount = aguardandoCount,
                isEngineRunning = isEngineRunning,
                currentCountdown = currentCountdown,
                onToggleEngine = handleAlternarMotor
            )
        }

        // 2. Linha de Ações Rápidas (Chips Pílula Translúcidos)
        item {
            QuickActionsChipsRow(
                aguardandoCount = aguardandoCount,
                activeSim = activeSim,
                isEngineRunning = isEngineRunning,
                onAgendarClick = onAgendarClick,
                onVerFilaClick = onVerFilaClick,
                onAlternarSim = onAlternarSim,
                onProcessarProximo = handleProcessarProximo
            )
        }

        // 3. Grande Card Glassmorphic: "Saldo Disponível & Cota de Envios" (Inspirado na referência IMG_8485.png)
        item {
            LargeQuotaGlassCard(
                activeSim = activeSim,
                activeSimInfo = activeSimInfo,
                onAlternarSim = onAlternarSim
            )
        }

        // 4. Card de Controle Operacional do Motor USSD
        item {
            EngineControlGlassCard(
                isEngineRunning = isEngineRunning,
                currentCountdown = currentCountdown,
                aguardandoCount = aguardandoCount,
                connectionStatus = connectionStatus,
                onToggleEngine = handleAlternarMotor,
                onVerFilaClick = onVerFilaClick,
                onAgendarClick = onAgendarClick
            )
        }

        // 5. Grid 2x2 Glassmorphic de Métricas
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassMetricCard(
                        title = "FILA ATIVA",
                        value = "$aguardandoCount",
                        subValue = if (emProcessamentoCount > 0) "1 em processo" else "Pendentes",
                        badge = if (aguardandoCount > 0) "EM FILA" else "VAZIO",
                        accentColor = if (aguardandoCount > 0) Bl4ckSecondary else Bl4ckTextMuted,
                        icon = Icons.Default.FormatListBulleted,
                        modifier = Modifier.weight(1f),
                        onClick = onVerFilaClick
                    )

                    GlassMetricCard(
                        title = "CONCLUÍDOS",
                        value = "$concluidosCount",
                        subValue = if (falhasCount > 0) "$falhasCount falhas" else "100% sucesso",
                        badge = "HISTÓRICO",
                        accentColor = Bl4ckPrimary,
                        icon = Icons.Default.History,
                        modifier = Modifier.weight(1f),
                        onClick = onVerHistoricoClick
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassMetricCard(
                        title = "SIM 1 CHIP",
                        value = "${sim1Info.remainingSends}",
                        subValue = "${sim1Info.carrierName} (lim ${sim1Info.totalLimit})",
                        badge = if (activeSim == 1) "ATIVO" else "SIM 1",
                        accentColor = if (activeSim == 1) Bl4ckPrimary else Bl4ckTextMuted,
                        icon = Icons.Default.SimCard,
                        modifier = Modifier.weight(1f),
                        onClick = onAlternarSim
                    )

                    GlassMetricCard(
                        title = "SIM 2 CHIP",
                        value = "${sim2Info.remainingSends}",
                        subValue = "${sim2Info.carrierName} (lim ${sim2Info.totalLimit})",
                        badge = if (activeSim == 2) "ATIVO" else "SIM 2",
                        accentColor = if (activeSim == 2) Bl4ckPrimary else Bl4ckTextMuted,
                        icon = Icons.Default.SimCard,
                        modifier = Modifier.weight(1f),
                        onClick = onAlternarSim
                    )
                }
            }
        }

        // 6. Última Execução e Resposta da Operadora (se houver)
        if (ultimoHistorico != null) {
            item {
                LatestExecutionGlassCard(
                    item = ultimoHistorico,
                    onVerHistoricoClick = onVerHistoricoClick
                )
            }
        }
    }
}

/**
 * 1. Hero Section: Ambient Liquid Glass Sphere & Mission Control
 * Inspirado visualmente na imagem de referência IMG_8485.png.
 */
@Composable
private fun HeroLiquidGlassSphere(
    aguardandoCount: Int,
    isEngineRunning: Boolean,
    currentCountdown: Int,
    onToggleEngine: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sphere_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Esfera 3D com Ambient Glow Laranja/Âmbar
        Box(
            modifier = Modifier
                .size(190.dp)
                .clickable { onToggleEngine() },
            contentAlignment = Alignment.Center
        ) {
            // Brilho Atmosférico Radial Traseiro
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .alpha(pulseAlpha)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Bl4ckSphereOuterGlow,
                                Color(0x18FF6B00),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Corpo da Esfera de Vidro Translúcido com Efeito 3D
            Box(
                modifier = Modifier
                    .size(155.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF281912),
                                Color(0xFF19110D),
                                Color(0xFF090706)
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.sweepGradient(
                            listOf(
                                Color(0x66FFFFFF),
                                Color(0x10FFFFFF),
                                Color(0x80F97316),
                                Color(0x10FFFFFF),
                                Color(0x66FFFFFF)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Reflexo de Luz Superior Especular (Curva do Vidro)
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 55.dp)
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x38FFFFFF),
                                    Color(0x05FFFFFF),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Cápsula Laranja Incandescente Central (Estilo [ 0 - ] da imagem de referência)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Bl4ckSphereInnerOrange,
                                    Bl4ckSphereInnerOrangeDark
                                )
                            )
                        )
                        .border(1.dp, Color(0x80FFB37A), RoundedCornerShape(18.dp))
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val displayText = if (isEngineRunning && currentCountdown > 0) {
                        "${currentCountdown}s -"
                    } else if (aguardandoCount > 0) {
                        "$aguardandoCount -"
                    } else {
                        "0 -"
                    }

                    Text(
                        text = displayText,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pílula de Status "● Serviço Iniciado" com Efeito Glow
        Surface(
            color = if (isEngineRunning) Bl4ckPillGreenBg else Color(0x26F59E0B),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                1.dp,
                if (isEngineRunning) Bl4ckPillGreenBorder else Color(0x59F59E0B)
            ),
            modifier = Modifier.clickable { onToggleEngine() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .alpha(pulseAlpha)
                        .clip(CircleShape)
                        .background(if (isEngineRunning) Bl4ckPillGreen else Bl4ckWarning)
                )

                Text(
                    text = if (isEngineRunning) "● Serviço Iniciado" else "● Serviço Pausado",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEngineRunning) Bl4ckPillGreen else Bl4ckWarning,
                    letterSpacing = 0.2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Frase Marcante de Impacto (Estilo "Relaxa e confia!...")
        Text(
            text = if (isEngineRunning) {
                "Relaxa e confia! O BL4CK SYSTEM tá no modo automático \uD83D\uDE97"
            } else {
                "Modo Standby. Toque para iniciar o piloto automático ⚡"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Bl4ckTextPrimary,
            textAlign = TextAlign.Center,
            fontSize = 17.sp,
            lineHeight = 23.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * 2. Linha de Chips Rápidos de Ação (Agendar, Lista de espera, etc.)
 */
@Composable
private fun QuickActionsChipsRow(
    aguardandoCount: Int,
    activeSim: Int,
    isEngineRunning: Boolean,
    onAgendarClick: () -> Unit,
    onVerFilaClick: () -> Unit,
    onAlternarSim: () -> Unit,
    onProcessarProximo: () -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickActionChip(
            icon = Icons.Default.Add,
            label = "Agendar transf...",
            onClick = onAgendarClick
        )

        QuickActionChip(
            icon = Icons.Default.FormatListBulleted,
            label = "Lista de espera ($aguardandoCount)",
            badge = if (aguardandoCount > 0) "$aguardandoCount" else null,
            onClick = onVerFilaClick
        )

        QuickActionChip(
            icon = Icons.Default.SimCard,
            label = "SIM $activeSim Ativo",
            onClick = onAlternarSim
        )

        if (!isEngineRunning && aguardandoCount > 0) {
            QuickActionChip(
                icon = Icons.Default.FlashOn,
                label = "Disparar 1",
                accentColor = Bl4ckSecondary,
                onClick = onProcessarProximo
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    badge: String? = null,
    accentColor: Color = Bl4ckTextPrimary,
    onClick: () -> Unit
) {
    Surface(
        color = Bl4ckGlassSurfaceLight,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Bl4ckGlassBorder),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(15.dp)
            )

            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Bl4ckTextPrimary
            )

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Bl4ckPrimary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF041E15)
                    )
                }
            }
        }
    }
}

/**
 * 3. Grande Card Glassmorphic: "Saldo Disponível & Cota de Envios"
 * Inspirado diretamente no grande card inferior da imagem de referência IMG_8485.png.
 */
@Composable
private fun LargeQuotaGlassCard(
    activeSim: Int,
    activeSimInfo: SimInfo,
    onAlternarSim: () -> Unit
) {
    val totalLimit = if (activeSimInfo.totalLimit > 0) activeSimInfo.totalLimit else 100
    val usedSends = (totalLimit - activeSimInfo.remainingSends).coerceAtLeast(0)
    val progress = (usedSends.toFloat() / totalLimit.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckGlassSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    Bl4ckGlassBorder,
                    Bl4ckGlassBorderSubtle
                )
            ),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Linha do Cabeçalho: "Saldo disponível" + Tag do Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saldo disponível",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Bl4ckTextPrimary,
                    letterSpacing = (-0.2).sp
                )

                Surface(
                    color = Bl4ckGlassSurfaceLight,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Bl4ckGlassBorderSubtle),
                    modifier = Modifier.clickable { onAlternarSim() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${activeSimInfo.carrierName} SIM $activeSim",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Bl4ckSecondary
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Bl4ckTextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Destaque de Tipografia Forte: Quota Restante em Destaque
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${activeSimInfo.remainingSends}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (activeSimInfo.isLimitReached) Bl4ckError else Bl4ckTextPrimary,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = " / ${activeSimInfo.totalLimit} envios",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Bl4ckTextSecondary,
                            modifier = Modifier.padding(bottom = 5.dp, start = 6.dp)
                        )
                    }

                    Text(
                        text = if (activeSimInfo.isLimitReached) {
                            "Limite diário atingido! Alterne para o outro chip."
                        } else {
                            "Cota diária com proteção automática anti-bloqueio"
                        },
                        fontSize = 12.sp,
                        color = if (activeSimInfo.isLimitReached) Bl4ckError else Bl4ckTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Botão de Ação Circular Minimalista
                Surface(
                    shape = CircleShape,
                    color = Bl4ckGlassSurfaceLight,
                    border = BorderStroke(1.dp, Bl4ckGlassBorder),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { onAlternarSim() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Alternar SIM",
                            tint = Bl4ckPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de Progresso Fina e Sofisticada
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (activeSimInfo.isLimitReached) Bl4ckError else Bl4ckPrimary,
                trackColor = Color(0x331E2A3C)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${activeSimInfo.remainingSends} envios restantes hoje",
                    fontSize = 11.sp,
                    color = Bl4ckTextSecondary
                )
                Text(
                    text = "${(progress * 100).toInt()}% utilizado",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Bl4ckTextMuted
                )
            }
        }
    }
}

/**
 * 4. Card de Controle do Motor USSD e Automação
 */
@Composable
private fun EngineControlGlassCard(
    isEngineRunning: Boolean,
    currentCountdown: Int,
    aguardandoCount: Int,
    connectionStatus: ConnectionStatus,
    onToggleEngine: () -> Unit,
    onVerFilaClick: () -> Unit,
    onAgendarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckGlassSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    if (isEngineRunning) Bl4ckPrimary.copy(alpha = 0.3f) else Bl4ckGlassBorder,
                    Bl4ckGlassBorderSubtle
                )
            ),
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
                Column {
                    Text(
                        text = "Piloto Automático USSD",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Bl4ckTextPrimary
                    )
                    Text(
                        text = if (isEngineRunning) {
                            if (currentCountdown > 0) "Próximo envio em ${currentCountdown}s" else "Processando fila em segundo plano"
                        } else {
                            "Ciclo inteligente 4-8s pausado"
                        },
                        fontSize = 12.sp,
                        color = if (isEngineRunning) Bl4ckPrimary else Bl4ckTextMuted
                    )
                }

                Surface(
                    color = when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> Color(0x2610B981)
                        ConnectionStatus.CONNECTING -> Color(0x26F59E0B)
                        ConnectionStatus.DISCONNECTED -> Color(0x26EF4444)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        when (connectionStatus) {
                            ConnectionStatus.CONNECTED -> Color(0x5910B981)
                            ConnectionStatus.CONNECTING -> Color(0x59F59E0B)
                            ConnectionStatus.DISCONNECTED -> Color(0x59EF4444)
                        }
                    )
                ) {
                    Text(
                        text = when (connectionStatus) {
                            ConnectionStatus.CONNECTED -> "ONLINE"
                            ConnectionStatus.CONNECTING -> "SYNC..."
                            ConnectionStatus.DISCONNECTED -> "OFFLINE"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (connectionStatus) {
                            ConnectionStatus.CONNECTED -> Bl4ckPrimary
                            ConnectionStatus.CONNECTING -> Bl4ckWarning
                            ConnectionStatus.DISCONNECTED -> Bl4ckError
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onToggleEngine,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEngineRunning) Color(0xFF24151B) else Bl4ckPrimary,
                        contentColor = if (isEngineRunning) Bl4ckError else Bl4ckOnPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = if (isEngineRunning) BorderStroke(1.dp, Bl4ckError.copy(alpha = 0.5f)) else null,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("btn_toggle_engine")
                ) {
                    Icon(
                        imageVector = if (isEngineRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = if (isEngineRunning) "Pausar Motor" else "Iniciar Motor",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                OutlinedButton(
                    onClick = onVerFilaClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Bl4ckGlassSurfaceLight,
                        contentColor = Bl4ckTextPrimary
                    ),
                    border = BorderStroke(1.dp, Bl4ckGlassBorder),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp)
                ) {
                    Text(
                        text = "Ver Fila ($aguardandoCount)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * 5. Card Individual de Métrica Glassmorphic
 */
@Composable
private fun GlassMetricCard(
    title: String,
    value: String,
    subValue: String,
    badge: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckGlassSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(Bl4ckGlassBorder, Bl4ckGlassBorderSubtle)
            ),
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Bl4ckTextMuted,
                    letterSpacing = 0.6.sp
                )

                Surface(
                    color = accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subValue,
                fontSize = 11.sp,
                color = Bl4ckTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 6. Card de Última Execução e Resposta da Operadora
 */
@Composable
private fun LatestExecutionGlassCard(
    item: HistoricoItem,
    onVerHistoricoClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckGlassSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(Bl4ckGlassBorder, Bl4ckGlassBorderSubtle)
            ),
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
                Text(
                    text = "Última Resposta Registrada",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Bl4ckTextPrimary
                )

                Text(
                    text = item.formattedTime,
                    fontSize = 11.sp,
                    color = Bl4ckTextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.numeroDestino,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Bl4ckTextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${item.megas} MB • SIM ${item.simSlot}",
                        fontSize = 12.sp,
                        color = Bl4ckTextSecondary
                    )
                }

                StatusBadge(status = item.status)
            }

            if (item.ussdResposta.isNotBlank()) {
                val limpo = item.ussdResposta.trim()
                    .replace(Regex("(?i)\\b(ok|fechar|close|cancelar|send|enviar|dismiss|entendido)\\b"), "")
                    .replace(Regex("\\s+"), " ").trim()

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF090D15))
                        .border(1.dp, Bl4ckGlassBorderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
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
                            fontSize = 12.sp,
                            color = Bl4ckSecondary,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "CONCLUIDO" -> Triple(Color(0x2610B981), Bl4ckPrimary, "SUCESSO")
        "FALHA" -> Triple(Color(0x26EF4444), Bl4ckError, "FALHA")
        "PROCESSANDO" -> Triple(Color(0x2638BDF8), Bl4ckSecondary, "ENVIANDO")
        else -> Triple(Color(0x26F59E0B), Bl4ckWarning, status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
