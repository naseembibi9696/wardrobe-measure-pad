package com.wardrobemeasure.pad.presentation.compare

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.navigation.Screen
import com.wardrobemeasure.pad.core.ui.*
import com.wardrobemeasure.pad.domain.model.*
import com.wardrobemeasure.pad.presentation.common.*

class CompareActions(val back: () -> Unit, val profile: (String) -> Unit, val mode: (MeasureMode) -> Unit,
    val garment: (String) -> Unit, val chart: (String) -> Unit, val navigate: (Screen) -> Unit,
    val update: () -> Unit, val compare: () -> Unit)

@Composable
fun CompareScreen(state: CompareUiState, actions: CompareActions) {
    NotebookPage(R.string.compare, actions.back, bottom = {
        val readiness = stringResource(if (state.ready) R.string.ready_compare else R.string.missing_fields)
        Text(stringResource(R.string.compare_hint), style = MaterialTheme.typography.bodyMedium)
        PrimaryButton(if (state.error) R.string.retry else R.string.compare_action, actions.compare,
            modifier = Modifier.semantics { stateDescription = readiness }, enabled = state.ready && !state.busy)
    }) {
        if (state.loading || state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (state.error) StatusMessage(R.string.storage_error)
        if (!state.loading && state.profiles.isEmpty()) {
            StatusMessage(R.string.compare_no_people)
            PrimaryButton(R.string.create_person, { actions.navigate(Screen.ProfileEditor()) })
        } else {
            NotebookCard {
                Text(stringResource(R.string.source), style = MaterialTheme.typography.titleLarge)
                state.profiles.forEach { person -> ChoiceChip(person.name, person.id == state.profileId, { actions.profile(person.id) }, Modifier.fillMaxWidth(), !state.busy) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceChip(stringResource(R.string.body_action), state.mode == MeasureMode.BODY, { actions.mode(MeasureMode.BODY) }, Modifier.weight(1f), !state.busy)
                    ChoiceChip(stringResource(R.string.garments_action), state.mode == MeasureMode.GARMENT, { actions.mode(MeasureMode.GARMENT) }, Modifier.weight(1f), !state.busy)
                }
                if (state.mode == MeasureMode.GARMENT) {
                    if (state.garments.isEmpty()) { StatusMessage(R.string.no_garments); SecondaryButton(R.string.add_garment, actions.update) }
                    state.garments.forEach { garment -> ChoiceChip(garment.name, garment.id == state.garmentId, { actions.garment(garment.id) }, Modifier.fillMaxWidth(), !state.busy) }
                }
            }
            NotebookCard {
                Text(stringResource(R.string.chart), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.source_basis_notice), style = MaterialTheme.typography.bodyMedium)
                if (state.charts.isEmpty()) {
                    StatusMessage(R.string.no_eligible_charts)
                    SecondaryButton(R.string.create_chart, { actions.navigate(Screen.ChartEditor(basis = if (state.mode == MeasureMode.BODY) ChartBasis.BODY else ChartBasis.FINISHED_GARMENT)) })
                }
                state.charts.forEach { chart ->
                    ChoiceChip(stringResource(R.string.garment_meta, chart.brand, chart.title), chart.id == state.chartId, { actions.chart(chart.id) }, Modifier.fillMaxWidth(), !state.busy)
                    if (chart.id == state.chartId) Text(stringResource(chart.basis.label), style = MaterialTheme.typography.labelSmall)
                }
            }
            if (state.keys.isNotEmpty()) NotebookCard {
                Text(stringResource(R.string.required_measurements), style = MaterialTheme.typography.titleLarge)
                state.keys.forEach { key ->
                    Text(stringResource(key.label), style = MaterialTheme.typography.titleMedium)
                    val raw = state.values[key]
                    if (raw != null && key.isFlatWidth(state.mode)) Text(stringResource(R.string.derived_value,
                        lengthText(raw, state.unit), lengthText(canonicalValue(key, raw, state.mode), state.unit)))
                    else Text(lengthText(raw, state.unit))
                }
                StatusMessage(if (state.missing.isEmpty()) R.string.ready_compare else R.string.missing_fields)
                SecondaryButton(R.string.update_measurements, actions.update, Modifier.fillMaxWidth(), !state.busy)
            }
        }
    }
}
