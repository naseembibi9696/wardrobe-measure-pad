package com.wardrobemeasure.pad.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.core.theme.*

@Composable
fun MeasurementGuideFigure(drawable: Int, description: String) {
    NotebookCard {
        Box(Modifier.fillMaxWidth().height(224.dp), contentAlignment = Alignment.Center) {
            Image(painterResource(drawable), description, Modifier.size(172.dp), contentScale = ContentScale.Fit)
            Canvas(Modifier.fillMaxWidth().height(200.dp)) {
                val y = size.height - 12.dp.toPx()
                drawLine(Primary.copy(alpha = 0.5f), Offset(0f, y), Offset(size.width, y), 2.dp.toPx())
                for (i in 0..24) {
                    val x = size.width * i / 24f
                    drawLine(if (i % 4 == 0) Accent else Primary.copy(alpha = 0.5f), Offset(x, y), Offset(x, y - (if (i % 4 == 0) 14.dp else 7.dp).toPx()), 2.dp.toPx())
                }
            }
        }
    }
}
