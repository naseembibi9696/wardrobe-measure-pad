package com.wardrobemeasure.pad.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.ui.*
import com.wardrobemeasure.pad.domain.model.UnitSystem

@Composable
fun SettingsScreen(state: SettingsUiState, onBack: () -> Unit, onUnit: (UnitSystem) -> Unit, onHaptics: (Boolean) -> Unit, onMenu: () -> Unit, onRetry: () -> Unit) {
    NotebookPage(R.string.settings, onBack, bottom = { SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth()) }) {
        NotebookCard {
            Text(stringResource(R.string.units_title), style = MaterialTheme.typography.titleLarge)
            listOf(UnitSystem.CM to R.string.centimetres, UnitSystem.IN to R.string.imperial).forEach { (unit, label) ->
                val status = stringResource(if (state.unit == unit) R.string.selected else R.string.not_selected)
                val description = stringResource(label)
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { stateDescription = status }, verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(state.unit == unit, { onUnit(unit) }, enabled = !state.busy,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics { contentDescription = description })
                    Text(stringResource(label), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        NotebookCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.haptics), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.haptics_hint), style = MaterialTheme.typography.bodyMedium)
                }
                val status = stringResource(if (state.haptics) R.string.enabled else R.string.disabled)
                val description = stringResource(R.string.haptics)
                Switch(state.haptics, onHaptics, enabled = !state.busy, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics { stateDescription = status; contentDescription = description })
            }
        }
        NotebookCard {
            Text(stringResource(R.string.privacy_title), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.privacy_hint), style = MaterialTheme.typography.bodyLarge)
        }
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (state.saved) StatusMessage(R.string.settings_saved)
        if (state.error) { StatusMessage(R.string.storage_error); SecondaryButton(R.string.retry, onRetry) }
    }
}
