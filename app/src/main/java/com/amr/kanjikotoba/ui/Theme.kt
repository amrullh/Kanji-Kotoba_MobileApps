package com.amr.kanjikotoba.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF1F3A5F)
val NavyDark = Color(0xFF16293F)
val Accent = Color(0xFF2E7D8F)
val SoftBlue = Color(0xFFE8F0F7)
val SoftGreen = Color(0xFFE6F4EA)
val SoftAmber = Color(0xFFFFF4E0)

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    secondary = Accent,
    background = Color(0xFFF7F9FC),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E)
)

@Composable
fun KanjiKotobaTheme(content: @Composable () -> Unit) {
    @Suppress("UNUSED_EXPRESSION")
    isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
