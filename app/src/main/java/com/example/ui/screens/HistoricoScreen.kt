package com.example.ui.screens

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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoricoItem
import com.example.ui.theme.Bl4ckBackground
import com.example.ui.theme.Bl4ckBorder
import com.example.ui.theme.Bl4ckBorderSubtle
import com.example.ui.theme.Bl4ckError
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
fun HistoricoScreen(
    historicoList: List<HistoricoItem>,
    onLimparHistorico: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("TODOS") }

    val filteredList = remember(historicoList, selectedFilter) {
        when (selectedFilter) {
            "CONCLUIDO" -> historicoList.filter { it.status == "CONCLUIDO" }
            "ANALISE" -> historicoList.filter { it.status == "ANALISE" }
            "FALHA" -> historicoList.filter { it.status == "FALHA" }
            "CANCELADO" -> historicoList.filter { it.status == "CANCELADO" }
            else -> historicoList
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Bl4ckBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header com Título e Ação de Limpar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HISTÓRICO DE OPERAÇÕES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Bl4ckTextPrimary
                    )
                    Text(
                        text = "Respostas diretas da operadora de telecomunicações",
                        style = MaterialTheme.typography.bodySmall,
                        color = Bl4ckTextSecondary
                    )
                }

                if (historicoList.isNotEmpty()) {
                    IconButton(
                        onClick = onLimparHistorico,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Bl4ckSurfaceVariant)
                            .border(1.dp, Bl4ckBorderSubtle, RoundedCornerShape(10.dp))
                            .testTag("btn_clear_history")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Limpar Histórico",
                            tint = Bl4ckTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Filtros (Chips modernos)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "TODOS" to "Todos (${historicoList.size})",
                    "CONCLUIDO" to "Concluídos",
                    "ANALISE" to "Análise",
                    "FALHA" to "Falhas"
                ).forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        shape = RoundedCornerShape(10.dp),
                        label = {
                            Text(
                                label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Bl4ckSurface,
                            selectedContainerColor = Bl4ckPrimary.copy(alpha = 0.14f),
                            labelColor = Bl4ckTextSecondary,
                            selectedLabelColor = Bl4ckPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Bl4ckBorderSubtle,
                            selectedBorderColor = Bl4ckPrimary.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        // Lista de Cards de Histórico
        if (filteredList.isEmpty()) {
            item {
                EmptyHistoricoView()
            }
        } else {
            items(filteredList, key = { it.id }) { item ->
                HistoricoCard(item = item)
            }
        }
    }
}

@Composable
fun HistoricoCard(
    item: HistoricoItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_historico_${item.id}"),
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
                .padding(15.dp)
        ) {
            // Linha 1: Badge de Status e Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HistoricoStatusBadge(status = item.status)

                Text(
                    text = item.formattedTime,
                    fontSize = 11.sp,
                    color = Bl4ckTextMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Linha 2: Número de Destino e Volume de Dados Transferido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.numeroDestino,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Bl4ckTextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${item.displayId} • SIM ${item.simSlot}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Bl4ckSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Bl4ckSurfaceVariant)
                        .border(1.dp, Bl4ckBorderSubtle, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${item.megas} MB",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Bl4ckTextPrimary
                    )
                }
            }

            // Linha 3: Resposta da operadora capturada pelo serviço de acessibilidade
            if (item.ussdResposta.isNotBlank()) {
                val respostaFormatada = formatarRespostaOperadoraExibicao(item.ussdResposta)
                val isSucessoPadrao = respostaFormatada.lowercase().contains("transferiste com sucesso") ||
                        respostaFormatada.lowercase().contains("transferido com sucesso")

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF090E17))
                        .border(
                            1.dp,
                            if (item.status == "ANALISE") Color(0xFFF59E0B).copy(alpha = 0.4f) else Bl4ckBorderSubtle,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RESPOSTA DA OPERADORA:",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Bl4ckTextMuted,
                                letterSpacing = 0.5.sp
                            )

                            if (item.status == "ANALISE" || !isSucessoPadrao && item.status != "FALHA") {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF332005))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ANÁLISE",
                                        color = Color(0xFFF59E0B),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = respostaFormatada,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (item.status == "ANALISE") Color(0xFFFDE68A) else Color(0xFFCBD5E1),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatarRespostaOperadoraExibicao(texto: String): String {
    var limpo = texto.trim()
    if (limpo.contains("Etapa") || limpo.contains("Step")) {
        limpo = limpo.substringAfterLast(":").trim()
    }
    val buttonRegex = Regex("(?i)\\b(ok|fechar|close|cancelar|send|enviar|dismiss|entendido)\\b")
    limpo = limpo.replace(buttonRegex, "").trim()
    limpo = limpo.replace(Regex("\\s+"), " ").trim()
    val finalTexto = limpo.ifBlank { texto.trim() }
    // No histórico a parte da resposta da operadora nunca deve ter mais de 70 carácteres se for corta o resto
    return if (finalTexto.length > 70) finalTexto.take(70).trim() else finalTexto
}

@Composable
fun HistoricoStatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "CONCLUIDO" -> Triple(
            Bl4ckPrimary.copy(alpha = 0.12f),
            Bl4ckPrimary,
            "CONCLUÍDO"
        )
        "ANALISE" -> Triple(
            Color(0xFFF59E0B).copy(alpha = 0.12f),
            Color(0xFFF59E0B),
            "ANÁLISE"
        )
        "FALHA" -> Triple(
            Bl4ckError.copy(alpha = 0.12f),
            Bl4ckError,
            "FALHA"
        )
        else -> Triple(
            Bl4ckWarning.copy(alpha = 0.12f),
            Bl4ckWarning,
            "CANCELADO"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun EmptyHistoricoView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Bl4ckSurface)
            .border(1.dp, Bl4ckBorder, RoundedCornerShape(18.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Bl4ckSurfaceVariant)
                .border(1.dp, Bl4ckBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Bl4ckTextMuted,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Histórico de Transações Vazio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Bl4ckTextPrimary
        )

        Text(
            text = "As confirmações e respostas USSD da operadora capturadas durante as execuções aparecerão aqui com timestamp preciso.",
            style = MaterialTheme.typography.bodySmall,
            color = Bl4ckTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
