package com.wardrobemeasure.pad.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.core.theme.*

@Composable
fun ActionPanel(content: @Composable () -> Unit) {
    Surface(shadowElevation = 6.dp, shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        border = BorderStroke(1.dp, Secondary.copy(alpha = 0.7f)), color = Background.copy(alpha = 0.96f)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}
