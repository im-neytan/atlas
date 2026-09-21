package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Bl4ckColorScheme = darkColorScheme(
    primary = Bl4ckPrimary,
    onPrimary = Bl4ckOnPrimary,
    primaryContainer = Bl4ckPrimaryContainer,
    onPrimaryContainer = Bl4ckPrimary,
    secondary = Bl4ckSecondary,
    secondaryContainer = Bl4ckSecondaryContainer,
    onSecondary = Bl4ckBackground,
    tertiary = Bl4ckTertiary,
    background = Bl4ckBackground,
    surface = Bl4ckSurface,
    surfaceVariant = Bl4ckSurfaceVariant,
    onBackground = Bl4ckTextPrimary,
    onSurface = Bl4ckTextPrimary,
    onSurfaceVariant = Bl4ckTextSecondary,
    outline = Bl4ckBorder,
    error = Bl4ckError,
    errorContainer = Bl4ckErrorContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Bl4ck System is a signature cyber dark UI
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Bl4ckBackground.toArgb()
                window.navigationBarColor = Bl4ckBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = Bl4ckColorScheme,
        typography = Typography,
        content = content
    )
}

