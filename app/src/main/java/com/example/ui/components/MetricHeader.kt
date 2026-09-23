package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.Bl4ckBorder
import com.example.ui.theme.Bl4ckBorderSubtle
import com.example.ui.theme.Bl4ckPrimary
import com.example.ui.theme.Bl4ckSecondary
import com.example.ui.theme.Bl4ckSurface
import com.example.ui.theme.Bl4ckSurfaceVariant
import com.example.ui.theme.Bl4ckTextMuted
import com.example.ui.theme.Bl4ckTextPrimary
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Bl4ckSurfaceVariant, Bl4ckSurface)
                )
            )
            .border(1.dp, Bl4ckBorder, RoundedCornerShape(20.dp))
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
                        .size(8.dp)
                        .alpha(pulseAlpha)
                        .clip(CircleShape)
                        .background(Bl4ckPrimary)
                )
                Text(
                    text = "MÉTRICAS DA FILA EM TEMPO REAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Bl4ckTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Bl4ckPrimary.copy(alpha = 0.12f))
                    .border(1.dp, Bl4ckPrimary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "LIVE TELEMETRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Bl4ckPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
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

@Composable
private fun ModernMetricCard(
    title: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isPulsing: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Bl4ckSurface)
            .border(1.dp, accentColor.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Bl4ckTextMuted,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
