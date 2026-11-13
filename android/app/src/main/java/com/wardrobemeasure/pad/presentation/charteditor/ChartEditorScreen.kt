package com.wardrobemeasure.pad.presentation.charteditor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.ui.*
import com.wardrobemeasure.pad.domain.model.*
import com.wardrobemeasure.pad.presentation.common.*
import com.wardrobemeasure.pad.presentation.dialog.*

class ChartEditorActions(val back: () -> Unit, val brand: (String) -> Unit, val title: (String) -> Unit,
    val basis: (ChartBasis) -> Unit, val category: (Category) -> Unit, val toggle: (FieldKey) -> Unit,
    val rowLabel: (String, String) -> Unit, val addRow: () -> Unit, val move: (String, Int) -> Unit, val askRemove: (String) -> Unit,
    val remove: () -> Unit, val cancelRemove: () -> Unit, val range: (String, FieldKey, Boolean) -> Unit,
    val pickerText: (String) -> Unit, val pickerStep: (Int) -> Unit, val pickerDone: () -> Unit, val pickerCancel: () -> Unit,
    val save: () -> Unit, val discard: () -> Unit, val cancelBack: () -> Unit)

@Composable
fun ChartEditorScreen(state: ChartEditorUiState, actions: ChartEditorActions) {
    val focus = remember { FocusRequester() }
    val titleFocus = remember { FocusRequester() }
    LaunchedEffect(state.invalid) {
        if (state.invalid && state.invalidRow == null) {
            if (state.brand.trim().length !in 1..40) focus.requestFocus()
            else if (state.title.trim().length !in 1..40) titleFocus.requestFocus()
        }
    }
    NotebookPage(if (state.editing) R.string.chart_editor else R.string.new_chart, actions.back, bottom = {
        Text(stringResource(R.string.chart_save_hint), style = MaterialTheme.typography.bodyMedium)
        PrimaryButton(if (state.error) R.string.retry_save else R.string.save_chart, actions.save, enabled = !state.busy && !state.loading)
    }) {
        if (state.loading || state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        NotebookCard {
            OutlinedTextField(state.brand, actions.brand, label = { Text(stringResource(R.string.brand)) },
                modifier = Modifier.fillMaxWidth().focusRequester(focus), singleLine = true, enabled = !state.busy, isError = state.invalid && state.brand.trim().length !in 1..40)
            OutlinedTextField(state.title, actions.title, label = { Text(stringResource(R.string.chart_name)) }, modifier = Modifier.fillMaxWidth().focusRequester(titleFocus),
                singleLine = true, enabled = !state.busy, isError = state.invalid && state.title.trim().length !in 1..40)
            ChartBasis.entries.forEach { basis -> ChoiceChip(stringResource(basis.label), state.basis == basis, { actions.basis(basis) }, Modifier.fillMaxWidth(), !state.busy) }
            Text(stringResource(R.string.category), style = MaterialTheme.typography.titleMedium)
            Category.entries.filter { state.basis == ChartBasis.BODY || it != Category.NECKWEAR }.forEach { category ->
                ChoiceChip(stringResource(category.label), state.category == category, { actions.category(category) }, Modifier.fillMaxWidth(), !state.busy)
            }
        }
        NotebookCard {
            Text(stringResource(R.string.comparison_fields), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.chart_keys_hint), style = MaterialTheme.typography.bodyMedium)
            chartFields(state.category, state.basis).forEach { key ->
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    val label = stringResource(key.label)
                    Checkbox(key in state.keys, { actions.toggle(key) }, enabled = !state.busy, modifier = Modifier.semantics { contentDescription = label })
                    Text(label, modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Text(stringResource(R.string.value_unit, stringResource(state.unit.label)), style = MaterialTheme.typography.labelSmall)
        state.rows.forEachIndexed { index, row ->
            val rowFocus = remember(row.id) { FocusRequester() }
            LaunchedEffect(state.invalid, state.invalidRow) { if (state.invalid && state.invalidRow == row.id) rowFocus.requestFocus() }
            NotebookCard(selected = state.invalid && state.invalidRow == row.id) {
                Text(stringResource(R.string.size_row, index + 1), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(row.label, { actions.rowLabel(row.id, it) }, label = { Text(stringResource(R.string.size_label)) },
                    modifier = Modifier.fillMaxWidth().focusRequester(rowFocus), enabled = !state.busy, singleLine = true,
                    isError = state.invalid && state.invalidRow == row.id && state.invalidKey == null)
                if (state.invalid && state.invalidRow == row.id && state.invalidKey == null) StatusMessage(R.string.row_label_error)
                state.keys.forEach { key ->
                    Text(stringResource(key.label), style = MaterialTheme.typography.bodyLarge)
                    val range = row.ranges[key]
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(false, true).forEach { maximum ->
                            val bound = stringResource(if (maximum) R.string.maximum_action else R.string.minimum_action)
                            val value = lengthText(if (maximum) range?.max else range?.min, state.unit)
                            val description = stringResource(R.string.range_control, row.label, stringResource(key.label), bound, value)
                            OutlinedButton(onClick = { actions.range(row.id, key, maximum) }, enabled = !state.busy,
                                modifier = Modifier.weight(1f).heightIn(min = 56.dp).semantics { contentDescription = description }) {
                                Column { Text(stringResource(R.string.data_text, bound), style = MaterialTheme.typography.labelSmall)
                                    Text(stringResource(R.string.data_text, value), style = MaterialTheme.typography.bodyLarge) }
                            }
                        }
                    }
                    if (state.invalid && state.invalidRow == row.id && state.invalidKey == key)
                        Text(stringResource(R.string.range_error, row.label, stringResource(key.label)), modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(R.string.move_up, { actions.move(row.id, -1) }, Modifier.weight(1f), !state.busy && index > 0)
                    SecondaryButton(R.string.move_down, { actions.move(row.id, 1) }, Modifier.weight(1f), !state.busy && index < state.rows.lastIndex)
                }
                SecondaryButton(R.string.remove_row, { actions.askRemove(row.id) }, enabled = !state.busy && state.rows.size > 1)
            }
        }
        SecondaryButton(R.string.add_size_row, actions.addRow, Modifier.fillMaxWidth(), !state.busy && state.rows.size < 12)
        if (state.invalid) StatusMessage(R.string.chart_validation)
        if (state.error) StatusMessage(R.string.storage_error)
    }
    state.picker?.let { NumberPickerDialog(it, actions.pickerText, actions.pickerStep, actions.pickerDone, actions.pickerCancel) }
    state.rows.find { it.id == state.removeRowId }?.let { ConfirmDeleteDialog(it.label, R.string.delete_row_detail, actions.remove, actions.cancelRemove) }
    if (state.confirmBack) UnsavedChangesDialog(R.string.save_chart, actions.save, actions.discard, actions.cancelBack)
}
