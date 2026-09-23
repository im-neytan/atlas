package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PedidoFila
import com.example.ui.components.MetricHeader
import com.example.ui.theme.Bl4ckBackground
import com.example.ui.theme.Bl4ckBorder
import com.example.ui.theme.Bl4ckBorderSubtle
import com.example.ui.theme.Bl4ckError
import com.example.ui.theme.Bl4ckGlassBorder
import com.example.ui.theme.Bl4ckGlassBorderSubtle
import com.example.ui.theme.Bl4ckGlassSurface
import com.example.ui.theme.Bl4ckGlassSurfaceLight
import com.example.ui.theme.Bl4ckOnPrimary
import com.example.ui.theme.Bl4ckPrimary
import com.example.ui.theme.Bl4ckSecondary
import com.example.ui.theme.Bl4ckSurface
import com.example.ui.theme.Bl4ckSurfaceElevated
import com.example.ui.theme.Bl4ckSurfaceVariant
import com.example.ui.theme.Bl4ckTextMuted
import com.example.ui.theme.Bl4ckTextPrimary
import com.example.ui.theme.Bl4ckTextSecondary
import com.example.ui.theme.Bl4ckWarning

@Composable
fun FilaScreen(
    pedidos: List<PedidoFila>,
    totalCount: Int,
    aguardandoCount: Int,
    emProcessamentoCount: Int,
    isEngineRunning: Boolean,
    currentCountdown: Int,
    onAgendarClick: () -> Unit,
    onAlternarMotor: () -> Unit,
    onProcessarProximo: () -> Unit,
    onLimparFila: () -> Unit,
    onExcluirPedido: (PedidoFila) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Bl4ckBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAgendarClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)) },
                text = {
                    Text(
                        text = "Nova Transferência",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.3.sp
                    )
                },
                containerColor = Bl4ckPrimary,
                contentColor = Bl4ckOnPrimary,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .testTag("fab_agendar_transferencia")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contadores no Topo
            item {
                MetricHeader(
                    total = totalCount,
                    aguardando = aguardandoCount,
                    emProcessamento = emProcessamentoCount
                )
            }

            // Barra de Controle da Fila (Motor & Ações Rápidas) em Card Glassmorphic
            item {
                QueueControlBar(
                    isRunning = isEngineRunning,
                    countdown = currentCountdown,
                    hasPending = aguardandoCount > 0,
                    onToggle = onAlternarMotor,
                    onProcessSingle = onProcessarProximo,
                    onClearQueue = onLimparFila
                )
            }

            // Cabeçalho da Lista
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FILA DE TRANSMISSÃO (${pedidos.size})",
                        fontSize = 12.sp,
                        color = Bl4ckTextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Surface(
                        color = Bl4ckGlassSurfaceLight,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Bl4ckGlassBorderSubtle)
                    ) {
                        Text(
                            text = "Vodacom *162#",
                            fontSize = 11.sp,
                            color = Bl4ckSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Lista ou Estado Vazio
            if (pedidos.isEmpty()) {
                item {
                    EmptyFilaView(onAgendarClick = onAgendarClick)
                }
            } else {
                items(pedidos, key = { it.id }) { pedido ->
                    PedidoFilaCard(
                        pedido = pedido,
                        onDelete = { onExcluirPedido(pedido) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueControlBar(
    isRunning: Boolean,
    countdown: Int,
    hasPending: Boolean,
    onToggle: () -> Unit,
    onProcessSingle: () -> Unit,
    onClearQueue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckGlassSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    if (isRunning) Bl4ckPrimary.copy(alpha = 0.35f) else Bl4ckGlassBorder,
                    Bl4ckGlassBorderSubtle
                )
            ),
            width = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFF24151B) else Bl4ckPrimary,
                    contentColor = if (isRunning) Bl4ckError else Bl4ckOnPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                border = if (isRunning) BorderStroke(1.dp, Bl4ckError.copy(alpha = 0.5f)) else null,
                modifier = Modifier
                    .weight(1.4f)
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isRunning) {
                        if (countdown > 0) "Pausar (${countdown}s)" else "Pausar Motor"
                    } else {
                        "Executar Fila"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            if (!isRunning && hasPending) {
                OutlinedButton(
                    onClick = onProcessSingle,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Bl4ckGlassSurfaceLight,
                        contentColor = Bl4ckSecondary
                    ),
                    border = BorderStroke(1.dp, Bl4ckSecondary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text("Disparar 1", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            IconButton(
                onClick = onClearQueue,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Bl4ckGlassSurfaceLight)
                    .border(1.dp, Bl4ckGlassBorder, RoundedCornerShape(14.dp))
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Limpar Fila",
                    tint = Bl4ckTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun PedidoFilaCard(
    pedido: PedidoFila,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProcessing = pedido.status == "EM PROCESSAMENTO"

    val cleanMegas = pedido.megas.replace(Regex("[^0-9]"), "").ifBlank { "500" }
    var cleanNum = pedido.numeroDestino.replace(Regex("[^0-9]"), "")
    if (cleanNum.startsWith("258") && cleanNum.length > 9) cleanNum = cleanNum.removePrefix("258")
    val previewUssd = "*162# ➔ 8 ➔ 2 ➔ ${cleanMegas}MB ➔ $cleanNum"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_pedido_${pedido.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckGlassSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    if (isProcessing) Bl4ckSecondary.copy(alpha = 0.6f) else Bl4ckGlassBorder,
                    Bl4ckGlassBorderSubtle
                )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = pedido.displayId,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Bl4ckSecondary
                    )

                    Surface(
                        color = Bl4ckGlassSurfaceLight,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Bl4ckGlassBorderSubtle)
                    ) {
                        Text(
                            text = "SIM ${pedido.simSlot} • Vodacom",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Bl4ckTextSecondary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                StatusBadge(status = pedido.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = pedido.numeroDestino,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Bl4ckTextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Moçambique (+258)",
                        fontSize = 11.sp,
                        color = Bl4ckTextMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Bl4ckPrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Bl4ckPrimary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "${pedido.megas} MB",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Bl4ckPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Bl4ckGlassSurfaceLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remover",
                            tint = Bl4ckTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090D15))
                    .border(1.dp, Bl4ckGlassBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = previewUssd,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Bl4ckSecondary,
                        fontWeight = FontWeight.Normal
                    )

                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(13.dp),
                            color = Bl4ckSecondary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFilaView(onAgendarClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Bl4ckGlassSurfaceLight)
                    .border(1.dp, Bl4ckGlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inbox,
                    contentDescription = null,
                    tint = Bl4ckPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fila de Transmissão Vazia",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Bl4ckTextPrimary
            )

            Text(
                text = "Nenhum pedido pendente para envio. Os novos agendamentos locais ou ordens remotas do painel web aparecerão aqui em tempo real.",
                style = MaterialTheme.typography.bodySmall,
                color = Bl4ckTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            Button(
                onClick = onAgendarClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Bl4ckPrimary,
                    contentColor = Bl4ckOnPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(
                    text = "Agendar Transferência",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}
