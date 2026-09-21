package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Bl4ckBackground
import com.example.ui.theme.Bl4ckBorder
import com.example.ui.theme.Bl4ckOnPrimary
import com.example.ui.theme.Bl4ckPrimary
import com.example.ui.theme.Bl4ckSecondary
import com.example.ui.theme.Bl4ckSurface
import com.example.ui.theme.Bl4ckSurfaceVariant
import com.example.ui.theme.Bl4ckTextMuted
import com.example.ui.theme.Bl4ckTextPrimary
import com.example.ui.theme.Bl4ckTextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AgendarTransferenciaBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirm: (numero: String, megas: String) -> Unit
) {
    var numero by remember { mutableStateOf("+258 84 ") }
    var megas by remember { mutableStateOf("500") }
    var numeroError by remember { mutableStateOf(false) }
    var megasError by remember { mutableStateOf(false) }

    val presetMegas = listOf("100", "200", "500", "1000", "2000", "5000")
    val presetPrefixes = listOf("+258 84", "+258 85", "+258 82", "+258 87")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Bl4ckSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header do BottomSheet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Nova Transferência",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Bl4ckTextPrimary
                    )
                    Text(
                        text = "Vodacom Moçambique (+258)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Bl4ckTextSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_schedule_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Bl4ckTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Campo: Número do Destinatário
            Text(
                text = "Número do Destinatário (Vodacom MZ)",
                style = MaterialTheme.typography.labelMedium,
                color = Bl4ckSecondary,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = numero,
                onValueChange = {
                    numero = it
                    numeroError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .testTag("input_recipient_number"),
                placeholder = { Text("+258 84 123 4567", color = Bl4ckTextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Bl4ckSecondary)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = numeroError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Bl4ckPrimary,
                    unfocusedBorderColor = Bl4ckBorder,
                    focusedTextColor = Bl4ckTextPrimary,
                    unfocusedTextColor = Bl4ckTextPrimary,
                    focusedContainerColor = Bl4ckSurfaceVariant,
                    unfocusedContainerColor = Bl4ckSurfaceVariant
                )
            )

            // Atalhos de prefixos de operadoras
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetPrefixes.forEach { prefix ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Bl4ckSurfaceVariant)
                            .border(1.dp, Bl4ckBorder, RoundedCornerShape(6.dp))
                            .clickable { numero = "$prefix " }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = prefix,
                            color = Bl4ckTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Campo: Quantidade de Megas
            Text(
                text = "Quantidade de Megas / Pacote",
                style = MaterialTheme.typography.labelMedium,
                color = Bl4ckPrimary,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = megas,
                onValueChange = {
                    megas = it
                    megasError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .testTag("input_megas_amount"),
                placeholder = { Text("Ex: 500 MB ou 2 GB", color = Bl4ckTextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.DataUsage, contentDescription = null, tint = Bl4ckPrimary)
                },
                singleLine = true,
                isError = megasError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Bl4ckPrimary,
                    unfocusedBorderColor = Bl4ckBorder,
                    focusedTextColor = Bl4ckTextPrimary,
                    unfocusedTextColor = Bl4ckTextPrimary,
                    focusedContainerColor = Bl4ckSurfaceVariant,
                    unfocusedContainerColor = Bl4ckSurfaceVariant
                )
            )

            // Presets rápidos de megas
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetMegas.forEach { preset ->
                    val isSelected = megas.equals(preset, ignoreCase = true)
                    SuggestionChip(
                        onClick = { megas = preset },
                        label = { Text(preset, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isSelected) Bl4ckPrimary.copy(alpha = 0.2f) else Bl4ckSurfaceVariant,
                            labelColor = if (isSelected) Bl4ckPrimary else Bl4ckTextSecondary
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = if (isSelected) Bl4ckPrimary else Bl4ckBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("cancel_schedule_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Bl4ckTextSecondary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        val numClean = numero.trim()
                        val megClean = megas.trim()
                        if (numClean.length < 5) {
                            numeroError = true
                            return@Button
                        }
                        if (megClean.isBlank()) {
                            megasError = true
                            return@Button
                        }
                        onConfirm(numClean, megClean)
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("confirm_schedule_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Bl4ckPrimary,
                        contentColor = Bl4ckOnPrimary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Text(
                        text = "Confirmar Agendamento",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}
