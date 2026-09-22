package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

/**
 * Tela de abertura com estética suave Aura Tech:
 * - Logo sem letras com raio e aura de luz ciano/esmeralda pulsante
 * - Textos calmos e elegantes em gradiente suave
 * - Rodapé exclusivo: "C 2026 bl4ck_solutions. Inc."
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Transição suave para liberar a tela principal após animação
    LaunchedEffect(Unit) {
        delay(2200)
        onSplashFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "aura_transition")

    // Pulso sutil da aura tecnológica
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_scale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    // Rotação sutil do gradiente de fundo tecnológico
    val auraGlowSpread by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_spread"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF071B16), // Fundo profundo esmeralda aura
                        Color(0xFF050B10), // Gradiente intermediário cyber
                        Color(0xFF020406)  // Fundo grafite profundo
                    )
                )
            )
            .testTag("screen_splash"),
        contentAlignment = Alignment.Center
    ) {
        // Aura suave de luz no fundo
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(auraScale)
                .blur(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = auraAlpha * 0.7f),
                            Color(0xFF06B6D4).copy(alpha = auraAlpha * 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Conteúdo Central: Logo Aura Tech e Título Calmo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Container circular da Logo Tech com borda sutil neon
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF10B981).copy(alpha = 0.8f),
                                Color(0xFF06B6D4).copy(alpha = 0.5f),
                                Color(0xFF10B981).copy(alpha = 0.2f),
                                Color(0xFF10B981).copy(alpha = 0.8f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .background(Color(0xFF061014)),
                contentAlignment = Alignment.Center
            ) {
                // Brilho interior
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF10B981).copy(alpha = 0.25f),
                                    Color(0xFF06B6D4).copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Imagem da Logo Aura Tech (Raio futurista sem letras)
                Image(
                    painter = painterResource(id = R.drawable.aura_tech_lightning_logo_1790083123480),
                    contentDescription = "Logo Aura Tech",
                    modifier = Modifier
                        .size(118.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Subtítulo calmo e suave
            Text(
                text = "bl4ck automation",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "sincronização & fluidez",
                fontSize = 11.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp,
                color = Color(0xFF64748B)
            )
        }

        // Rodapé Calmo e Suave solicitado: "C 2026 bl4ck_solutions. Inc."
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "C 2026 bl4ck_solutions. Inc.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.2.sp,
                color = Color(0xFF475569) // Cinza ardósia suave e sereno
            )
        }
    }
}
