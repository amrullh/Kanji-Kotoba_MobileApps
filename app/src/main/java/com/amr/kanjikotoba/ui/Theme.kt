package com.amr.kanjikotoba.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Warm dark palette: low contrast, muted accents, no harsh white.
val Ink = Color(0xFF171310)
val InkElevated = Color(0xFF211C18)
val InkCard = Color(0xFF2C2620)
val Cream = Color(0xFFF2EBE2)
val CreamMuted = Color(0xFFA89F94)
val Terracotta = Color(0xFFE07856)
val TerracottaSoft = Color(0xFF3A2A24)
val Sage = Color(0xFF8FA98F)
val SageSoft = Color(0xFF26312A)
val Sand = Color(0xFFC9B79C)

private val DarkColors = darkColorScheme(
    primary = Terracotta,
    onPrimary = Color(0xFF1A1310),
    primaryContainer = TerracottaSoft,
    onPrimaryContainer = Color(0xFFF5C9B4),
    secondary = Sage,
    onSecondary = Color(0xFF15201A),
    secondaryContainer = SageSoft,
    onSecondaryContainer = Color(0xFFC3D6C3),
    background = Ink,
    onBackground = Cream,
    surface = InkCard,
    onSurface = Cream,
    surfaceVariant = InkElevated,
    onSurfaceVariant = CreamMuted,
    outline = Color(0xFF4A423A),
    outlineVariant = Color(0xFF332C26)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFA8563A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF7E3D8),
    onPrimaryContainer = Color(0xFF5C2A17),
    secondary = Color(0xFF5F7A62),
    secondaryContainer = Color(0xFFE1EEE2),
    background = Color(0xFFFBF7F1),
    surface = Color.White,
    onSurface = Color(0xFF221C17)
)

@Composable
fun KanjiKotobaTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
