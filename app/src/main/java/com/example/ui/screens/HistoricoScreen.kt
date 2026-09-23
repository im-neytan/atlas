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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.HistoricoItem
import com.example.ui.theme.Bl4ckBackground
import com.example.ui.theme.Bl4ckBorderSubtle
import com.example.ui.theme.Bl4ckError
import com.example.ui.theme.Bl4ckGlassBorder
import com.example.ui.theme.Bl4ckGlassBorderSubtle
import com.example.ui.theme.Bl4ckGlassSurface
import com.example.ui.theme.Bl4ckGlassSurfaceLight
import com.example.ui.theme.Bl4ckPrimary
import com.example.ui.theme.Bl4ckSecondary
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
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Bl4ckTextPrimary
                    )
                    Text(
                        text = "Respostas diretas da operadora de telecomunicações",
                        fontSize = 12.sp,
                        color = Bl4ckTextSecondary
                    )
                }

                if (historicoList.isNotEmpty()) {
                    IconButton(
                        onClick = onLimparHistorico,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Bl4ckGlassSurfaceLight)
                            .border(1.dp, Bl4ckGlassBorder, RoundedCornerShape(14.dp))
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

        // Filtros (Chips modernos Glassmorphic)
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
                        shape = RoundedCornerShape(14.dp),
                        label = {
                            Text(
                                label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Bl4ckGlassSurfaceLight,
                            selectedContainerColor = Bl4ckPrimary.copy(alpha = 0.16f),
                            labelColor = Bl4ckTextSecondary,
                            selectedLabelColor = Bl4ckPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Bl4ckGlassBorderSubtle,
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
                .padding(16.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

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

                Surface(
                    color = Bl4ckGlassSurfaceLight,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Bl4ckGlassBorderSubtle)
                ) {
                    Text(
                        text = "${item.megas} MB",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Bl4ckTextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Linha 3: Resposta da operadora capturada pelo serviço de acessibilidade
            if (item.ussdResposta.isNotBlank()) {
                val respostaFormatada = formatarRespostaOperadoraExibicao(item.ussdResposta)
                val isSucessoPadrao = respostaFormatada.lowercase().contains("transferiste com sucesso") ||
                        respostaFormatada.lowercase().contains("transferido com sucesso")

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF090E17))
                        .border(
                            1.dp,
                            if (item.status == "ANALISE") Color(0xFFF59E0B).copy(alpha = 0.4f) else Bl4ckGlassBorderSubtle,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RESPOSTA DA OPERADORA:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Bl4ckTextMuted,
                                letterSpacing = 0.5.sp
                            )

                            if (item.status == "ANALISE" || !isSucessoPadrao && item.status != "FALHA") {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
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

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
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
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Bl4ckTextMuted,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
