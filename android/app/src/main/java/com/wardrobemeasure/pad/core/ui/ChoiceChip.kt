package com.wardrobemeasure.pad.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.theme.*

@Composable
fun ChoiceChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val status = stringResource(if (selected) R.string.selected else R.string.not_selected)
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp).semantics { stateDescription = status },
        shape = RoundedCornerShape(14.dp), border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Primary else Secondary),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) Secondary.copy(alpha = 0.55f) else Background.copy(alpha = 0.8f), contentColor = Ink)) {
        Text(stringResource(R.string.data_text, text), style = MaterialTheme.typography.labelLarge)
    }
}
