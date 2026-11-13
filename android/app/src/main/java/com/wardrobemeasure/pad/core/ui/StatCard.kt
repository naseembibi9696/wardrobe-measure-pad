package com.wardrobemeasure.pad.core.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wardrobemeasure.pad.R

@Composable
fun StatCard(label: Int, value: Int, modifier: Modifier = Modifier) {
    NotebookCard(modifier) {
        Crossfade(targetState = value, animationSpec = tween(160), label = "count") { Text(stringResource(R.string.number_text, it), style = MaterialTheme.typography.titleLarge) }
        Text(stringResource(label), style = MaterialTheme.typography.labelSmall)
    }
}
