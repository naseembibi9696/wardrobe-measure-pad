package com.wardrobemeasure.pad.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.core.theme.*

@Composable
fun SecondaryButton(label: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, if (enabled) Primary.copy(alpha = 0.55f) else Secondary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink, containerColor = Background.copy(alpha = 0.72f), disabledContainerColor = Secondary.copy(alpha = 0.4f))) {
        Text(stringResource(label), style = MaterialTheme.typography.labelLarge)
    }
}
