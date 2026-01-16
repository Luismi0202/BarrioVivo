package com.example.barriovivo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    secondary = OrangePrimary,
    tertiary = GreenDark,
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2A2A2A),
    error = ErrorRed,
    onError = TextLight
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = OrangePrimary,
    tertiary = GreenDark,
    background = BackgroundLight,
    surface = SurfaceLight,
    error = ErrorRed,
    onPrimary = TextLight,
    onSecondary = TextLight,
    onBackground = TextDark,
    onSurface = TextDark,
    onError = TextLight
)

@Composable
fun BarrioVivoTheme(
    darkTheme: Boolean = false, // Siempre forzamos tema claro
    dynamicColor: Boolean = false, // Desactivar dynamic color
    content: @Composable () -> Unit
) {
    // SIEMPRE usamos tema claro para consistencia
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = GreenPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}