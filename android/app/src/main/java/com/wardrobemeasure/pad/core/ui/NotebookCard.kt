package com.wardrobemeasure.pad.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.core.theme.*

@Composable
fun NotebookCard(modifier: Modifier = Modifier, selected: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val border by animateColorAsState(if (selected) Primary else Secondary.copy(alpha = 0.7f), tween(160), label = "selection")
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), shadowElevation = 3.dp,
        border = BorderStroke(if (selected) 2.dp else 1.dp, border),
        color = Background.copy(alpha = 0.95f)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}
