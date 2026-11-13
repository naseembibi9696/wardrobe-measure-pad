package com.wardrobemeasure.pad.core.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.R

@Composable
fun MeasurementRow(label: String, value: String, hint: Int, stepValue: String, recorded: Boolean, canDecrease: Boolean, canIncrease: Boolean,
    enabled: Boolean, onDecrease: () -> Unit, onIncrease: () -> Unit, onValue: () -> Unit, onClear: () -> Unit, onGuide: () -> Unit) {
    val status = stringResource(R.string.field_state, label, value)
    val decrease = stringResource(R.string.step_field, label, stringResource(R.string.decrease), stepValue)
    val increase = stringResource(R.string.step_field, label, stringResource(R.string.increase), stepValue)
    val lower = stringResource(R.string.lower_bound)
    val upper = stringResource(R.string.upper_bound)
    NotebookCard(modifier = Modifier.semantics { stateDescription = status }) {
        Text(stringResource(R.string.data_text, label), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(hint), style = MaterialTheme.typography.labelSmall)
        val decreaseModifier = Modifier.semantics {
            contentDescription = decrease
            if (recorded && !canDecrease) stateDescription = lower
        }
        val increaseModifier = Modifier.semantics {
            contentDescription = increase
            if (recorded && !canIncrease) stateDescription = upper
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth < 280.dp || LocalDensity.current.fontScale > 1.3f) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MeasurementValue(value, status, enabled, onValue, Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryButton(R.string.less, onDecrease, Modifier.weight(1f).then(decreaseModifier), enabled && (!recorded || canDecrease))
                        SecondaryButton(R.string.more, onIncrease, Modifier.weight(1f).then(increaseModifier), enabled && (!recorded || canIncrease))
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(R.string.less, onDecrease, decreaseModifier, enabled && (!recorded || canDecrease))
                    MeasurementValue(value, status, enabled, onValue, Modifier.weight(1f))
                    SecondaryButton(R.string.more, onIncrease, increaseModifier, enabled && (!recorded || canIncrease))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(R.string.guide_action, onGuide, Modifier.weight(1f), enabled)
            SecondaryButton(R.string.clear, onClear, Modifier.weight(1f), enabled && recorded)
        }
    }
}

@Composable
private fun MeasurementValue(value: String, status: String, enabled: Boolean, onValue: () -> Unit, modifier: Modifier) {
    OutlinedButton(onClick = onValue, enabled = enabled, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
        modifier = modifier.heightIn(min = 56.dp).semantics { contentDescription = status }) {
        Crossfade(targetState = value, animationSpec = tween(120), label = "measurement") {
            Text(stringResource(R.string.data_text, it), style = MaterialTheme.typography.titleLarge)
        }
    }
}
