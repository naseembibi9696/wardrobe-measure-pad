package com.wardrobemeasure.pad.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.core.theme.*

@Composable
fun PrimaryButton(label: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, spring(dampingRatio = 0.85f, stiffness = 650f), label = "press")
    val shape = RoundedCornerShape(16.dp)
    Button(onClick = onClick, enabled = enabled, interactionSource = interaction,
        shape = shape, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Ink,
            disabledContainerColor = Secondary, disabledContentColor = Ink.copy(alpha = 0.48f)),
        modifier = modifier.fillMaxWidth().heightIn(min = 60.dp).scale(scale)
            .shadow(if (enabled) 4.dp else 0.dp, shape).clip(shape)
            .background(Brush.horizontalGradient(if (enabled) listOf(Accent, lerp(Accent, Background, 0.18f)) else listOf(Secondary, Secondary)))) {
        Text(stringResource(label), style = MaterialTheme.typography.labelLarge)
    }
}
