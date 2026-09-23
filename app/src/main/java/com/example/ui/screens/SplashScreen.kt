package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
 * Tela de abertura (Splash Screen) oficial:
 * - Fundo totalmente preto (#000000) sem gradientes ou outras cores
 * - Logo oficial branca (#FFFFFF) centralizada mantendo rigorosamente proporções e desenho
 * - Rodapé sutil monocromático
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2200)
        onSplashFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse_transition")

    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .testTag("screen_splash"),
        contentAlignment = Alignment.Center
    ) {
        // Logo branca oficial centralizada no fundo preto puro
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "Logo Oficial",
                modifier = Modifier
                    .size(150.dp)
                    .scale(logoScale)
                    .testTag("splash_app_logo"),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "BL4CK AUTOMATION",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp,
                color = Color(0xFFFFFFFF)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "sincronização & fluidez",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 2.sp,
                color = Color(0xFF94A3B8)
            )
        }

        // Rodapé oficial discreto
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "© 2026 bl4ck_solutions.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.2.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
