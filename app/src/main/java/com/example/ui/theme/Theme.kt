package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CoralOrangePrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = CoralOrangePrimary,
    onPrimaryContainer = Color.White,
    secondary = AmberSecondaryDark,
    onSecondary = Color.Black,
    secondaryContainer = AmberSecondary,
    tertiary = VegGreen,
    background = SlateDarkBackground,
    surface = SlateDarkSurface,
    surfaceVariant = SlateDarkSurfaceVariant,
    onBackground = Color(0xFFF1F1F5),
    onSurface = Color(0xFFF1F1F5),
    onSurfaceVariant = Color(0xFFB0B0C0),
    outline = Color(0xFF3E3E48)
)

private val LightColorScheme = lightColorScheme(
    primary = CoralOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = PeachContainer,
    onPrimaryContainer = OnPeachContainer,
    secondary = AmberSecondary,
    onSecondary = Color.Black,
    secondaryContainer = AmberContainer,
    tertiary = VegGreen,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
)

@Composable
fun SomiGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun BiteDashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = SomiGoTheme(darkTheme, dynamicColor, content)
