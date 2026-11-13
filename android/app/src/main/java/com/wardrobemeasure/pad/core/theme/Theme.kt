package com.wardrobemeasure.pad.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NotebookColors = lightColorScheme(
    primary = Primary, onPrimary = Background, primaryContainer = Secondary, onPrimaryContainer = Ink,
    secondary = Accent, onSecondary = Ink, secondaryContainer = Accent.copy(alpha = 0.2f), onSecondaryContainer = Ink,
    background = Background, onBackground = Ink, surface = Background, onSurface = Ink,
    surfaceVariant = Secondary.copy(alpha = 0.35f), onSurfaceVariant = Ink.copy(alpha = 0.76f),
    outline = Secondary, error = Ink, onError = Background, errorContainer = Accent.copy(alpha = 0.25f), onErrorContainer = Ink
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NotebookColors, typography = NotebookTypography, content = content)
}
