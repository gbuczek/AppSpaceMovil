package com.appspace.movil.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Colores del tema claro
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4285F4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E0FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = Color(0xFF34A853),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB7E5C5),
    onSecondaryContainer = Color(0xFF00210B),
    tertiary = Color(0xFFFBBC04),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFEF0B3),
    onTertiaryContainer = Color(0xFF3D2F00),
    error = Color(0xFFEA4335),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F)
)

// Colores del tema oscuro
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF669DF6),
    onPrimary = Color(0xFF003062),
    primaryContainer = Color(0xFF00458E),
    onPrimaryContainer = Color(0xFFD0E0FF),
    secondary = Color(0xFF6FCB8A),
    onSecondary = Color(0xFF003914),
    secondaryContainer = Color(0xFF005324),
    onSecondaryContainer = Color(0xFFB7E5C5),
    tertiary = Color(0xFFFCDC6E),
    onTertiary = Color(0xFF3D2F00),
    tertiaryContainer = Color(0xFF5A4600),
    onTertiaryContainer = Color(0xFFFEF0B3),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099)
)

/**
 * Tema de la aplicación
 */
@Composable
fun AppSpaceMovilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
