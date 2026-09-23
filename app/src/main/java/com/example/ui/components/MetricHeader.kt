package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Bl4ckGlassBorder
import com.example.ui.theme.Bl4ckGlassBorderSubtle
import com.example.ui.theme.Bl4ckGlassSurface
import com.example.ui.theme.Bl4ckGlassSurfaceLight
import com.example.ui.theme.Bl4ckPrimary
import com.example.ui.theme.Bl4ckSecondary
import com.example.ui.theme.Bl4ckTextMuted
import com.example.ui.theme.Bl4ckTextSecondary
import com.example.ui.theme.Bl4ckWarning

@Composable
fun MetricHeader(
    total: Int,
    aguardando: Int,
    emProcessamento: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .alpha(pulseAlpha)
                            .clip(CircleShape)
                            .background(Bl4ckPrimary)
                    )
                    Text(
                        text = "MÉTRICAS DA FILA EM TEMPO REAL",
                        fontSize = 11.sp,
                        color = Bl4ckTextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = Bl4ckPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Bl4ckPrimary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "LIVE TELEMETRY",
                        color = Bl4ckPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // TOTAL NA FILA
                ModernMetricCard(
                    title = "TOTAL NA FILA",
                    count = total,
                    accentColor = Bl4ckSecondary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("metric_total")
                )

                // AGUARDANDO
                ModernMetricCard(
                    title = "AGUARDANDO",
                    count = aguardando,
                    accentColor = Bl4ckWarning,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("metric_waiting")
                )

                // EM PROCESSAMENTO
                ModernMetricCard(
                    title = "EM PROCESSO",
                    count = emProcessamento,
                    accentColor = Bl4ckPrimary,
                    isPulsing = emProcessamento > 0,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("metric_processing")
                )
            }
        }
    }
}

@Composable
private fun ModernMetricCard(
    title: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isPulsing: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = Bl4ckGlassSurfaceLight,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Bl4ckTextMuted,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
