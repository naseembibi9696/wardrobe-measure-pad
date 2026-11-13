package com.wardrobemeasure.pad.presentation.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.ui.*
import com.wardrobemeasure.pad.domain.model.*
import com.wardrobemeasure.pad.presentation.common.*
import com.wardrobemeasure.pad.presentation.dialog.*

class GameActions(val back: () -> Unit, val mode: (MeasureMode) -> Unit, val garment: (String) -> Unit,
    val step: (FieldKey, Int) -> Unit, val picker: (FieldKey) -> Unit, val guide: (FieldKey) -> Unit, val clear: (FieldKey) -> Unit,
    val save: () -> Unit, val compare: () -> Unit, val units: (Boolean) -> Unit, val setUnit: (UnitSystem) -> Unit,
    val editGarment: (Boolean) -> Unit, val askDelete: (Boolean) -> Unit, val delete: () -> Unit,
    val pickerText: (String) -> Unit, val pickerStep: (Int) -> Unit, val pickerDone: () -> Unit, val pickerCancel: () -> Unit,
    val garmentName: (String) -> Unit, val garmentCategory: (Category) -> Unit, val garmentSave: (Boolean) -> Unit, val garmentCancel: () -> Unit,
    val pendingSave: () -> Unit, val discard: () -> Unit, val pendingCancel: () -> Unit, val retryDraft: () -> Unit)

@Composable
fun GameScreen(state: GameUiState, actions: GameActions) {
    NotebookPage(R.string.measurements, actions.back, bottom = {
        if (!state.missingSource && !state.loading) {
            Text(stringResource(if (state.values.isEmpty()) R.string.empty_measurements else R.string.save_hint), style = MaterialTheme.typography.bodyMedium)
            PrimaryButton(if (state.error) R.string.retry_save else R.string.save_measurements, actions.save,
                enabled = !state.busy && state.values.isNotEmpty() && (state.mode == MeasureMode.BODY || state.garmentId != null))
        }
    }) {
        if (state.loading) { LinearProgressIndicator(Modifier.fillMaxWidth()); StatusMessage(R.string.loading) }
        else if (state.missingSource) StatusMessage(R.string.not_available)
        else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.person_context, state.profileName), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                SecondaryButton(state.unit.label, { actions.units(true) }, enabled = !state.busy)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip(stringResource(R.string.body_action), state.mode == MeasureMode.BODY, { actions.mode(MeasureMode.BODY) }, Modifier.weight(1f), !state.busy)
                ChoiceChip(stringResource(R.string.garments_action), state.mode == MeasureMode.GARMENT, { actions.mode(MeasureMode.GARMENT) }, Modifier.weight(1f), !state.busy)
            }
            Text(stringResource(if (state.mode == MeasureMode.BODY) R.string.body_measurements else R.string.garment_measurements), style = MaterialTheme.typography.titleLarge)
            if (state.mode == MeasureMode.GARMENT) {
                if (state.garments.isEmpty()) StatusMessage(R.string.no_garments)
                state.garments.forEach { garment ->
                    val selected = garment.id == state.garmentId
                    NotebookCard(selected = selected) {
                        val label = stringResource(if (selected) R.string.selected else R.string.not_selected)
                        OutlinedButton(onClick = { actions.garment(garment.id) }, enabled = !state.busy,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics { stateDescription = label }) {
                            Text(stringResource(R.string.data_text, garment.name))
                        }
                        Text(stringResource(garment.category.label), style = MaterialTheme.typography.labelSmall)
                        if (garment.isSample) Text(stringResource(R.string.sample_badge), style = MaterialTheme.typography.labelSmall)
                    }
                }
                SecondaryButton(R.string.add_garment, { actions.editGarment(false) }, Modifier.fillMaxWidth(), !state.busy)
                if (state.garmentId != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryButton(R.string.edit_garment, { actions.editGarment(true) }, Modifier.weight(1f), !state.busy)
                        SecondaryButton(R.string.delete_garment, { actions.askDelete(true) }, Modifier.weight(1f), !state.busy)
                    }
                    Text(stringResource(R.string.flat_width_hint), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(painterResource(R.drawable.icon_pause), null, Modifier.size(24.dp), contentScale = ContentScale.Fit)
                StatusMessage(when { state.busy -> R.string.saving; state.draftSaved -> R.string.draft_saved; state.dirty -> R.string.draft_unsaved; else -> R.string.save_hint })
            }
            if (state.error) { StatusMessage(R.string.storage_error); SecondaryButton(R.string.retry, actions.retryDraft, enabled = !state.busy) }
            if (state.invalid) StatusMessage(R.string.invalid_measurements)
            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (state.mode == MeasureMode.BODY || state.garmentId != null) state.fields.forEach { key ->
                val value = state.values[key]
                val flat = key.isFlatWidth(state.mode)
                val hint = if (flat) R.string.flat_width else if (key in listOf(FieldKey.CHEST, FieldKey.WAIST, FieldKey.HIPS, FieldKey.NECK)) R.string.full_circumference else R.string.direct_length
                MeasurementRow(stringResource(key.label), lengthText(value, state.unit), hint, lengthText(state.unit.step, state.unit), value != null,
                    value?.let { MeasurementUnits.valid(it - state.unit.step, flat) } == true,
                    value?.let { MeasurementUnits.valid(it + state.unit.step, flat) } == true, !state.busy,
                    { actions.step(key, -1) }, { actions.step(key, 1) }, { actions.picker(key) }, { actions.clear(key) }, { actions.guide(key) })
            }
            SecondaryButton(R.string.compare_action, actions.compare, Modifier.fillMaxWidth(), !state.busy)
        }
    }
    state.picker?.let { NumberPickerDialog(it, actions.pickerText, actions.pickerStep, actions.pickerDone, actions.pickerCancel) }
    if (state.unitPicker) AlertDialog(onDismissRequest = { actions.units(false) }, title = { Text(stringResource(R.string.units_title)) },
        text = { Column { SecondaryButton(R.string.cm, { actions.setUnit(UnitSystem.CM) }); SecondaryButton(R.string.inches, { actions.setUnit(UnitSystem.IN) }) } },
        confirmButton = { SecondaryButton(R.string.cancel, { actions.units(false) }) })
    if (state.pending != null) UnsavedChangesDialog(if (state.pending == PendingAction.COMPARE) R.string.save_continue else R.string.save_measurements,
        actions.pendingSave, actions.discard, actions.pendingCancel)
    if (state.deleteGarment) ConfirmDeleteDialog(state.garments.find { it.id == state.garmentId }?.name.orEmpty(), R.string.delete_garment_detail,
        actions.delete, { actions.askDelete(false) })
    state.garmentEditor?.let { editor ->
        AlertDialog(onDismissRequest = actions.garmentCancel, title = { Text(stringResource(if (editor.id == null) R.string.add_garment else R.string.edit_garment)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(editor.name, actions.garmentName, label = { Text(stringResource(R.string.garment_name)) }, singleLine = true, isError = editor.error, enabled = !state.busy)
                    Text(stringResource(R.string.category))
                    listOf(Category.TOP, Category.BOTTOM, Category.FULL_BODY).forEach { category ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val label = stringResource(category.label)
                            RadioButton(category == editor.category, { actions.garmentCategory(category) }, enabled = !state.busy,
                                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics { contentDescription = label })
                            Text(stringResource(category.label))
                        }
                    }
                    if (editor.error) StatusMessage(R.string.garment_name_error)
                    if (editor.confirmPrune) StatusMessage(R.string.prune_warning)
                }
            }, confirmButton = { SecondaryButton(if (editor.confirmPrune) R.string.confirm_change else R.string.save_garment,
                { actions.garmentSave(editor.confirmPrune) }, enabled = !state.busy) }, dismissButton = { SecondaryButton(R.string.cancel, actions.garmentCancel, enabled = !state.busy) })
    }
}
