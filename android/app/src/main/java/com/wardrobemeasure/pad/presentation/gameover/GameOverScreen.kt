package com.wardrobemeasure.pad.presentation.gameover

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.theme.*
import com.wardrobemeasure.pad.core.ui.*
import com.wardrobemeasure.pad.domain.model.*
import com.wardrobemeasure.pad.presentation.common.*

@Composable
fun GameOverScreen(state: GameOverUiState, onAgain: () -> Unit, onMenu: () -> Unit, onCompare: () -> Unit, onEditChart: () -> Unit) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(state.result?.id) { appeared = state.result != null }
    val scale by animateFloatAsState(if (appeared) 1f else 0.85f, tween(240), label = "saved")
    val result = state.result
    NotebookPage(R.string.result_title, onAgain, Backdrop.RESULT, bottom = {
        Text(stringResource(R.string.result_hint), style = MaterialTheme.typography.bodyMedium)
        if (result != null) PrimaryButton(R.string.play_again, onAgain)
        SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth())
    }) {
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        else if (result == null) StatusMessage(R.string.stale_result)
        else {
            NotebookCard {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(72.dp).scale(scale)) {
                        drawCircle(Primary)
                        drawLine(Background, Offset(size.width * 0.28f, size.height * 0.51f), Offset(size.width * 0.44f, size.height * 0.66f), 4.dp.toPx())
                        drawLine(Background, Offset(size.width * 0.44f, size.height * 0.66f), Offset(size.width * 0.74f, size.height * 0.34f), 4.dp.toPx())
                    }
                }
                if (result.kind == ResultKind.SAVED) {
                    Text(stringResource(R.string.saved_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    Text(stringResource(R.string.data_text, result.sourceName), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.saved_on, dateText(result.timestamp)), style = MaterialTheme.typography.labelSmall)
                } else {
                    Text(if (result.chartBrand.isEmpty()) stringResource(R.string.data_text, result.chartTitle) else stringResource(R.string.garment_meta, result.chartBrand, result.chartTitle), style = MaterialTheme.typography.titleLarge)
                    result.chartBasis?.let { Text(stringResource(it.label), style = MaterialTheme.typography.labelSmall) }
                    Text(stringResource(R.string.data_text, result.sourceName), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.size_result, result.size), style = MaterialTheme.typography.displaySmall)
                    StatusMessage(if (result.matched) R.string.within_ranges else R.string.closest_row)
                    if (!result.matched) Text(stringResource(R.string.no_match), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.comparison_explainer), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (result.kind == ResultKind.SAVED) {
                NotebookCard {
                    result.values.forEach { (key, value) ->
                        Text(stringResource(key.label), style = MaterialTheme.typography.labelSmall)
                        Text(lengthText(value, state.unit), style = MaterialTheme.typography.titleLarge)
                        if (key.isFlatWidth(if (result.garmentId == null) MeasureMode.BODY else MeasureMode.GARMENT))
                            Text(stringResource(R.string.flat_width), style = MaterialTheme.typography.labelSmall)
                    }
                }
                Text(stringResource(R.string.saved_detail), style = MaterialTheme.typography.titleLarge)
                SecondaryButton(R.string.compare_action, onCompare, Modifier.fillMaxWidth())
            } else {
                result.outcomes.forEach { row ->
                    val outcome = when {
                        row.outcome == Outcome.WITHIN -> stringResource(R.string.within_range)
                        row.gap * 20 < state.unit.scale -> stringResource(if (row.outcome == Outcome.BELOW) R.string.small_below else R.string.small_above, stringResource(state.unit.label))
                        else -> stringResource(if (row.outcome == Outcome.BELOW) R.string.below_range else R.string.above_range, lengthText(row.gap, state.unit))
                    }
                    val range = stringResource(R.string.range_value, lengthText(row.range.min, state.unit), lengthText(row.range.max, state.unit))
                    val source = if (row.raw != row.value) stringResource(R.string.derived_value, lengthText(row.raw, state.unit), lengthText(row.value, state.unit)) else lengthText(row.value, state.unit)
                    RangeOutcomeRow(stringResource(row.key.label), source, range, outcome)
                }
                if (state.chartExists) SecondaryButton(R.string.edit_chart, onEditChart, Modifier.fillMaxWidth())
            }
        }
    }
}
