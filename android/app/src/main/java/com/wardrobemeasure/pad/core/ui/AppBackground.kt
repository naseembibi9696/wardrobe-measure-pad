package com.wardrobemeasure.pad.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.theme.*

enum class Backdrop { SPLASH, PEOPLE, EDITOR, RESULT }

@Composable
fun AppBackground(variant: Backdrop = Backdrop.EDITOR, content: @Composable BoxScope.() -> Unit) {
    val colors = when (variant) {
        Backdrop.SPLASH -> listOf(Primary.copy(alpha = 0.94f), Ink.copy(alpha = 0.96f), Ink.copy(alpha = 0.98f))
        Backdrop.PEOPLE -> listOf(Background.copy(alpha = 0.97f), Background.copy(alpha = 0.94f), Secondary.copy(alpha = 0.95f))
        Backdrop.EDITOR -> listOf(Background.copy(alpha = 0.97f), Secondary.copy(alpha = 0.94f), Background.copy(alpha = 0.97f))
        Backdrop.RESULT -> listOf(Secondary.copy(alpha = 0.95f), Background.copy(alpha = 0.96f), Background.copy(alpha = 0.97f))
    }
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_game), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
        if (variant == Backdrop.RESULT) Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Accent.copy(alpha = 0.12f), Accent.copy(alpha = 0f)))))
        content()
    }
}
