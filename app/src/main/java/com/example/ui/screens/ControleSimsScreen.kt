package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.SimCardAlert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SimCard
import com.example.data.model.SimInfo
import com.example.network.ConnectionStatus
import com.example.ui.theme.Bl4ckBackground
import com.example.ui.theme.Bl4ckBorder
import com.example.ui.theme.Bl4ckBorderSubtle
import com.example.ui.theme.Bl4ckError
import com.example.ui.theme.Bl4ckOnPrimary
import com.example.ui.theme.Bl4ckPrimary
import com.example.ui.theme.Bl4ckSecondary
import com.example.ui.theme.Bl4ckSurface
import com.example.ui.theme.Bl4ckSurfaceVariant
import com.example.ui.theme.Bl4ckTextMuted
import com.example.ui.theme.Bl4ckTextPrimary
import com.example.ui.theme.Bl4ckTextSecondary
import com.example.ui.theme.Bl4ckWarning

@Composable
fun ControleSimsScreen(
    activeSim: Int,
    sim1Info: SimInfo,
    sim2Info: SimInfo,
    connectionStatus: ConnectionStatus,
    serverUrl: String,
    onAlternarSim: () -> Unit,
    onConectarWebSocket: (String) -> Unit,
    onAlternarConexao: () -> Unit,
    onSimularComando: (String) -> Unit,
    onAtualizarLimiteManual: (simSlot: Int, limite: Int, restantes: Int) -> Unit,
    simCards: List<SimCard> = emptyList(),
    onAtualizarSims: () -> Unit = {},
    onSalvarNumeroSim: (slot: Int, numero: String) -> Unit = { _, _ -> },
    onAbrirConfiguracoesSistema: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var editUrl by remember(serverUrl) { mutableStateOf(serverUrl) }
    var editingSlot by remember { mutableStateOf<Int?>(null) }
    var editNumberValue by remember { mutableStateOf("") }

    // Obter ou construir o estado real para ambos os slots (Slot 1 e Slot 2)
    val sim1 = simCards.firstOrNull { it.slot == 1 } ?: SimCard(
        slot = 1,
        providerName = if (sim1Info.isInserted) sim1Info.carrierName else "Nenhum provedor",
        phoneNumber = "Não gravado no chip",
        status = if (sim1Info.isInserted) (if (activeSim == 1) "Ativo para Chamadas" else "Standby") else "Ausente / Slot Vazio",
        isInserted = sim1Info.isInserted,
        isActiveVoice = activeSim == 1,
        remainingSends = sim1Info.remainingSends,
        totalLimit = sim1Info.totalLimit
    )

    val sim2 = simCards.firstOrNull { it.slot == 2 } ?: SimCard(
        slot = 2,
        providerName = if (sim2Info.isInserted) sim2Info.carrierName else "Nenhum provedor",
        phoneNumber = "Não gravado no chip",
        status = if (sim2Info.isInserted) (if (activeSim == 2) "Ativo para Chamadas" else "Standby") else "Slot 2 Vazio / Nenhum cartão inserido",
        isInserted = sim2Info.isInserted,
        isActiveVoice = activeSim == 2,
        remainingSends = sim2Info.remainingSends,
        totalLimit = sim2Info.totalLimit
    )

    val totalSimsReais = listOf(sim1, sim2).count { it.isInserted }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Bl4ckBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // -------------------------------------------------------------
        // 1. INDICADOR VISUAL DO SIM ATIVO E STATUS
        // -------------------------------------------------------------
        item {
            ActiveSimHeaderCard(
                activeSim = activeSim,
                sim1Info = sim1Info,
                sim2Info = sim2Info,
                onAlternarSim = onAlternarSim,
                onAbrirConfiguracoesSistema = onAbrirConfiguracoesSistema
            )
        }

        // -------------------------------------------------------------
        // 2. ESTADO REAL DOS DOIS SLOTS DE SIM (2, 1 OU NENHUM)
        // -------------------------------------------------------------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ESTADO REAL DOS CARTÕES SIM (ROOM)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Bl4ckSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = when (totalSimsReais) {
                            2 -> "2 SIMs detectados no telefone (Dual SIM)"
                            1 -> "1 SIM detectado no telefone (Single SIM)"
                            else -> "Nenhum SIM detectado no telefone (0 cartões)"
                        },
                        fontSize = 11.sp,
                        color = if (totalSimsReais > 0) Bl4ckTextSecondary else Bl4ckError
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onAbrirConfiguracoesSistema,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Bl4ckSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Bl4ckBorderSubtle)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Config. SIM", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onAtualizarSims,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Bl4ckPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Bl4ckBorderSubtle)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reescanear", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Mostra o SIM 1 (sempre exibido para reportar estado real: inserido ou ausente)
        item {
            RealSimCardDetailCard(
                simCard = sim1,
                isActiveVoice = activeSim == 1 && sim1.isInserted,
                onAlternarSim = { if (activeSim != 1 && sim1.isInserted) onAlternarSim() },
                onAbrirConfiguracoesSistema = onAbrirConfiguracoesSistema,
                onEditarNumero = {
                    editingSlot = 1
                    editNumberValue = if (sim1.phoneNumber != "Não gravado no chip" && sim1.phoneNumber != "N/A") sim1.phoneNumber else ""
                },
                onResetLimits = { onAtualizarLimiteManual(1, 10, 10) }
            )
        }

        // Mostra o SIM 2 (sempre exibido para reportar estado real: inserido ou ausente)
        item {
            RealSimCardDetailCard(
                simCard = sim2,
                isActiveVoice = activeSim == 2 && sim2.isInserted,
                onAlternarSim = { if (activeSim != 2 && sim2.isInserted) onAlternarSim() },
                onAbrirConfiguracoesSistema = onAbrirConfiguracoesSistema,
                onEditarNumero = {
                    editingSlot = 2
                    editNumberValue = if (sim2.phoneNumber != "Não gravado no chip" && sim2.phoneNumber != "N/A") sim2.phoneNumber else ""
                },
                onResetLimits = { onAtualizarLimiteManual(2, 10, 10) }
            )
        }

        // -------------------------------------------------------------
        // 3. CONEXÃO WEBSOCKET COM O BOT CENTRAL
        // -------------------------------------------------------------
        item {
            ServerConnectionCard(
                status = connectionStatus,
                url = editUrl,
                onUrlChange = { editUrl = it },
                onConnectClick = { onConectarWebSocket(editUrl) },
                onToggleConnection = onAlternarConexao
            )
        }

        // -------------------------------------------------------------
        // 4. TESTE DE COMANDOS REMOTOS DO BOT
        // -------------------------------------------------------------
        item {
            BotCommandsSimulatorCard(
                onSimular = onSimularComando
            )
        }
    }

    // Diálogo para editar/cadastrar o número do SIM no Room
    if (editingSlot != null) {
        AlertDialog(
            onDismissRequest = { editingSlot = null },
            title = {
                Text(
                    text = "Número da Linha (SIM $editingSlot)",
                    fontWeight = FontWeight.Bold,
                    color = Bl4ckTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Insira o número do telefone associado a este chip para armazenar no Room:",
                        fontSize = 13.sp,
                        color = Bl4ckTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editNumberValue,
                        onValueChange = { editNumberValue = it },
                        label = { Text("Número (ex: +258 84 123 4567)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Bl4ckPrimary,
                            unfocusedBorderColor = Bl4ckBorderSubtle,
                            focusedTextColor = Bl4ckTextPrimary,
                            unfocusedTextColor = Bl4ckTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val slot = editingSlot ?: 1
                        onSalvarNumeroSim(slot, editNumberValue.trim())
                        editingSlot = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Bl4ckPrimary,
                        contentColor = Bl4ckOnPrimary
                    )
                ) {
                    Text("Salvar no Banco", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { editingSlot = null },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Bl4ckTextSecondary)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = Bl4ckSurface
        )
    }
}

@Composable
private fun ActiveSimHeaderCard(
    activeSim: Int,
    sim1Info: SimInfo,
    sim2Info: SimInfo,
    onAlternarSim: () -> Unit
) {
    val hasAnySim = sim1Info.isInserted || sim2Info.isInserted
    val isDualSim = sim1Info.isInserted && sim2Info.isInserted
    val currentCarrier = if (activeSim == 1) sim1Info.carrierName else sim2Info.carrierName

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_active_sim_indicator"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (hasAnySim) Bl4ckPrimary.copy(alpha = 0.4f) else Bl4ckBorderSubtle
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
                    Icon(
                        imageVector = if (hasAnySim) Icons.Default.SimCard else Icons.Default.SimCardAlert,
                        contentDescription = null,
                        tint = if (hasAnySim) Bl4ckPrimary else Bl4ckTextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (hasAnySim) "CANAL DE TRANSMISSÃO" else "ESTADO DOS CARTÕES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Bl4ckTextPrimary
                    )
                }

                Text(
                    text = when {
                        !hasAnySim -> "Sem cartões ativos"
                        isDualSim -> "Dual SIM Ativo"
                        else -> "SIM único ativo"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasAnySim) Bl4ckSecondary else Bl4ckError
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!hasAnySim) {
                // Mensagem organizada quando nenhum chip é detectado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Bl4ckSurfaceVariant)
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneDisabled,
                            contentDescription = null,
                            tint = Bl4ckTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Sem cartões ativos no telefone",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Bl4ckTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Nenhum chip físico ou eSIM foi detectado no aparelho. Insira um SIM card para disparar chamadas USSD.",
                                fontSize = 11.sp,
                                color = Bl4ckTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else if (isDualSim) {
                // Seletor Lado a Lado para Dual SIM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SimSlotButton(
                        slot = 1,
                        carrier = sim1Info.carrierName,
                        isActive = activeSim == 1,
                        remaining = "${sim1Info.remainingSends}/${sim1Info.totalLimit}",
                        isLimitReached = sim1Info.isLimitReached,
                        modifier = Modifier.weight(1f),
                        onClick = { if (activeSim != 1) onAlternarSim() }
                    )

                    SimSlotButton(
                        slot = 2,
                        carrier = sim2Info.carrierName,
                        isActive = activeSim == 2,
                        remaining = "${sim2Info.remainingSends}/${sim2Info.totalLimit}",
                        isLimitReached = sim2Info.isLimitReached,
                        modifier = Modifier.weight(1f),
                        onClick = { if (activeSim != 2) onAlternarSim() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botão Manual de Troca Rápida de SIM de Chamada
                Button(
                    onClick = onAlternarSim,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_alternar_sim"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Bl4ckSurfaceVariant,
                        contentColor = Bl4ckSecondary
                    )
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                    Text(
                        text = "Alternar Linha Principal (SIM 1 ⇄ SIM 2)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            } else {
                // Modo Chip Único Detectado
                val singleSim = if (sim1Info.isInserted) sim1Info else sim2Info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF132219))
                        .border(1.5.dp, Bl4ckPrimary, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SimCard,
                                    contentDescription = null,
                                    tint = Bl4ckPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = singleSim.carrierName,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Bl4ckTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Slot ${singleSim.slot} • Linha principal para chamadas e dados USSD",
                                fontSize = 12.sp,
                                color = Bl4ckTextSecondary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Bl4ckPrimary)
                            )
                            Text(
                                text = "Ativo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Bl4ckPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimSlotButton(
    slot: Int,
    carrier: String,
    isActive: Boolean,
    remaining: String,
    isLimitReached: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .testTag("btn_sim_slot_$slot")
            .clip(RoundedCornerShape(14.dp))
            .background(if (isActive) Color(0xFF132219) else Bl4ckSurfaceVariant)
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) Bl4ckPrimary else Bl4ckBorderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SimCard,
                        contentDescription = null,
                        tint = if (isActive) Bl4ckPrimary else Bl4ckTextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Slot $slot",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) Bl4ckPrimary else Bl4ckTextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Bl4ckPrimary else Bl4ckTextMuted.copy(alpha = 0.5f))
                    )
                    Text(
                        text = if (isActive) "Ativo" else "Espera",
                        fontSize = 11.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) Bl4ckPrimary else Bl4ckTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nome da Operadora com destaque hierárquico
            Text(
                text = carrier,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Bl4ckTextPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isLimitReached) "⚠️ Cota esgotada" else "$remaining envios",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isLimitReached) Bl4ckError else Bl4ckTextSecondary
            )
        }
    }
}

@Composable
private fun SimProgressCard(
    simInfo: SimInfo,
    onResetLimits: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_sim_progress_${simInfo.slot}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (simInfo.isLimitReached) Bl4ckError else Bl4ckBorderSubtle
            ),
            width = if (simInfo.isLimitReached) 1.5.dp else 1.dp
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
                        text = simInfo.carrierName,
                        fontWeight = FontWeight.ExtraBold,
                        color = Bl4ckTextPrimary,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Slot ${simInfo.slot} • ${if (simInfo.isActive) "Linha ativa de chamadas" else "Linha em espera (standby)"}",
                        fontSize = 12.sp,
                        color = Bl4ckTextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${simInfo.remainingSends}/${simInfo.totalLimit}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = if (simInfo.isLimitReached) Bl4ckError else Bl4ckPrimary
                    )
                    Text(
                        text = "envios restantes",
                        fontSize = 10.sp,
                        color = Bl4ckTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de Progresso elegante
            LinearProgressIndicator(
                progress = { simInfo.progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (simInfo.isLimitReached) Bl4ckError else Bl4ckPrimary,
                trackColor = Bl4ckSurfaceVariant
            )

            // Alerta visual caso o limite chegue a 0
            AnimatedVisibility(visible = simInfo.isLimitReached) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF381515))
                        .border(1.dp, Bl4ckError.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Bl4ckError,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Limite diário de transmissões atingido neste SIM.",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Bl4ckError
                        )
                    }
                }
            }

            // Ação de restaurar cota
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onResetLimits,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Bl4ckSecondary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Bl4ckBorderSubtle)
                ) {
                    Icon(Icons.Default.Cached, contentDescription = null, modifier = Modifier.size(13.dp))
                    Text(
                        text = "Restaurar Cota (10/10)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerConnectionCard(
    status: ConnectionStatus,
    url: String,
    onUrlChange: (String) -> Unit,
    onConnectClick: () -> Unit,
    onToggleConnection: () -> Unit
) {
    val isConnected = status == ConnectionStatus.CONNECTED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_server_connection"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Bl4ckSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isConnected) Bl4ckPrimary.copy(alpha = 0.4f) else Bl4ckBorderSubtle
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
                Text(
                    text = "INTEGRAÇÃO COM BOT CENTRAL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Bl4ckTextPrimary
                )

                // Indicador de Conexão WebSocket/HTTP
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.testTag("indicator_connection_status")
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) Bl4ckPrimary else Bl4ckError)
                    )
                    Text(
                        text = if (isConnected) "Conectado" else "Desconectado",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isConnected) Bl4ckPrimary else Bl4ckError
                    )
                }
            }

            Text(
                text = "Comunicação remota em tempo real via WebSocket com o Bot",
                fontSize = 11.sp,
                color = Bl4ckTextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Endereço do WebSocket", color = Bl4ckTextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Bl4ckSecondary,
                    unfocusedBorderColor = Bl4ckBorder,
                    focusedTextColor = Bl4ckTextPrimary,
                    unfocusedTextColor = Bl4ckTextPrimary,
                    focusedContainerColor = Bl4ckSurfaceVariant,
                    unfocusedContainerColor = Bl4ckSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onToggleConnection,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_toggle_connection"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) Bl4ckError else Bl4ckPrimary,
                    contentColor = if (isConnected) Color.White else Bl4ckOnPrimary
                )
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.CloudOff else Icons.Default.CloudDone,
                    contentDescription = null
                )
                Text(
                    text = if (isConnected) "Desconectar do Servidor" else "Conectar ao Bot Central",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun BotCommandsSimulatorCard(
    onSimular: (String) -> Unit
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
            Text(
                text = "DIAGNÓSTICO & SINCRONIZAÇÃO REMOTA",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Bl4ckSecondary
            )
            Text(
                text = "Testes de telemetria e sincronização em tempo real com o servidor:",
                fontSize = 11.sp,
                color = Bl4ckTextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onSimular("switch_sim") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("cmd_switch_sim")
                ) {
                    Text("Alternar Chip", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { onSimular("clear") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("cmd_clear_queue")
                ) {
                    Text("Limpar Fila", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { onSimular("get_status") },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("cmd_get_status")
            ) {
                Text("Disparar Telemetria de Estado ao Bot", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RealSimCardDetailCard(
    simCard: SimCard,
    isActiveVoice: Boolean,
    onAlternarSim: () -> Unit,
    onEditarNumero: () -> Unit,
    onResetLimits: () -> Unit
) {
    val isInserted = simCard.isInserted
    val statusColor = when {
        !isInserted -> Bl4ckTextMuted
        isActiveVoice -> Bl4ckPrimary
        else -> Bl4ckSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_sim_slot_${simCard.slot}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInserted) Bl4ckSurface else Bl4ckSurfaceVariant.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isActiveVoice) Bl4ckPrimary.copy(alpha = 0.8f) else Bl4ckBorderSubtle
            ),
            width = if (isActiveVoice) 1.5.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabeçalho: Slot & Indicador de Status Real
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isActiveVoice) Bl4ckPrimary else Bl4ckSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SLOT ${simCard.slot}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isActiveVoice) Color(0xFF0F172A) else Bl4ckTextSecondary
                        )
                    }

                    Text(
                        text = if (isInserted) simCard.providerName else "Nenhum Cartão",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isInserted) Bl4ckTextPrimary else Bl4ckTextMuted
                    )
                }

                // Badge de Status Real
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = when {
                            !isInserted -> "Slot Vazio"
                            isActiveVoice -> "Ativo p/ Chamadas"
                            else -> "Standby"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Detalhes: Provedor, Número de Telefone e Status Real
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Bl4ckSurfaceVariant)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nome do Provedor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Provedor:", fontSize = 12.sp, color = Bl4ckTextMuted)
                    Text(
                        text = if (isInserted) simCard.providerName else "Sem cartão no slot",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isInserted) Bl4ckTextPrimary else Bl4ckTextMuted
                    )
                }

                // Número de Telefone com botão de edição
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Número:", fontSize = 12.sp, color = Bl4ckTextMuted)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (simCard.phoneNumber.isNotBlank()) simCard.phoneNumber else "Não informado",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isInserted) Bl4ckPrimary else Bl4ckTextMuted
                        )
                        IconButton(
                            onClick = onEditarNumero,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar número do SIM ${simCard.slot}",
                                tint = Bl4ckSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                // Status Real Reportado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Status Real:", fontSize = 12.sp, color = Bl4ckTextMuted)
                    Text(
                        text = simCard.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            if (isInserted) {
                Spacer(modifier = Modifier.height(12.dp))

                // Cota de envios restantes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Envios restantes: ${simCard.remainingSends}/${simCard.totalLimit}",
                        fontSize = 12.sp,
                        color = Bl4ckTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (simCard.remainingSends == 0) "Limite atingido" else "${(simCard.remainingSends * 100) / simCard.totalLimit}%",
                        fontSize = 11.sp,
                        color = if (simCard.remainingSends == 0) Bl4ckError else Bl4ckSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val progress = (simCard.remainingSends.toFloat() / simCard.totalLimit.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (simCard.remainingSends == 0) Bl4ckError else Bl4ckPrimary,
                    trackColor = Bl4ckSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isActiveVoice) {
                        Button(
                            onClick = onAlternarSim,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Bl4ckPrimary,
                                contentColor = Color(0xFF0F172A)
                            )
                        ) {
                            Text("Definir como Principal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onResetLimits,
                        modifier = if (isActiveVoice) Modifier.fillMaxWidth() else Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Bl4ckTextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Bl4ckBorderSubtle)
                    ) {
                        Text("Resetar Cota (10)", fontSize = 12.sp)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nenhum cartão SIM detectado pelo hardware neste slot.",
                    fontSize = 11.sp,
                    color = Bl4ckTextMuted
                )
            }
        }
    }
}
