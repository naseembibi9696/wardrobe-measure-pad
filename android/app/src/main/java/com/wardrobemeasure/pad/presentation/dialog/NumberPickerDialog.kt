package com.wardrobemeasure.pad.presentation.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.ui.*
import com.wardrobemeasure.pad.domain.model.*
import com.wardrobemeasure.pad.presentation.common.*

@Composable
fun NumberPickerDialog(state: PickerState, onText: (String) -> Unit, onStep: (Int) -> Unit, onDone: () -> Unit, onCancel: () -> Unit) {
    val label = stringResource(state.field.label)
    val focus = remember { FocusRequester() }
    LaunchedEffect(state.field, state.rowId, state.maximum) { focus.requestFocus() }
    AlertDialog(onDismissRequest = onCancel, title = {
        Text(if (state.rowId == null) label else stringResource(R.string.picker_range_title,
            stringResource(if (state.maximum) R.string.maximum_action else R.string.minimum_action), label))
    }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Text(stringResource(if (state.flat) R.string.flat_width_hint else R.string.valid_range))
            OutlinedTextField(value = state.text, onValueChange = onText, label = { Text(stringResource(R.string.value_unit, stringResource(state.unit.label))) },
                isError = state.error, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().focusRequester(focus).semantics { stateDescription = label })
            if (state.error) StatusMessage(if (state.flat) R.string.invalid_flat else R.string.invalid_number)
            Row {
                SecondaryButton(R.string.decrease, { onStep(-1) }, Modifier.weight(1f), state.value()?.let { MeasurementUnits.valid(it - state.unit.step, state.flat) } == true)
                SecondaryButton(R.string.increase, { onStep(1) }, Modifier.weight(1f), state.value()?.let { MeasurementUnits.valid(it + state.unit.step, state.flat) } == true)
            }
        }
    }, confirmButton = { SecondaryButton(R.string.done, onDone) }, dismissButton = { SecondaryButton(R.string.cancel, onCancel) })
}
