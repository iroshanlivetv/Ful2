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
    primary = FuelBlueLight,
    onPrimary = Color.White,
    primaryContainer = FuelBlueDark,
    onPrimaryContainer = Color.White,
    secondary = FuelBluePrimary,
    onSecondary = Color.White,
    background = FuelNavyBackground,
    onBackground = Color.White,
    surface = FuelNavySurface,
    onSurface = Color.White,
    surfaceVariant = FuelNavyCard,
    onSurfaceVariant = Slate400,
    error = FuelRedAlert,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = FuelBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = FuelBlueDark,
    secondary = FuelBlueDark,
    onSecondary = Color.White,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    error = FuelRedAlert,
    onError = Color.White
)

@Composable
fun FuelPassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our brand FuelPass palette for distinctive identity
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
