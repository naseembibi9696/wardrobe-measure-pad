package com.wardrobemeasure.pad.presentation.charts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.navigation.Screen
import com.wardrobemeasure.pad.core.ui.*
import com.wardrobemeasure.pad.presentation.common.label
import com.wardrobemeasure.pad.presentation.dialog.ConfirmDeleteDialog

@Composable
fun SizeChartsScreen(state: SizeChartsUiState, onBack: () -> Unit, onNavigate: (Screen) -> Unit,
    onQuery: (String) -> Unit, onAskDelete: (String?) -> Unit, onDelete: () -> Unit, onRetry: () -> Unit) {
    NotebookPage(R.string.size_charts, onBack, bottom = { PrimaryButton(R.string.create_chart, { onNavigate(Screen.ChartEditor()) }, enabled = !state.busy) }) {
        Text(stringResource(R.string.charts_hint), style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(state.query, onQuery, label = { Text(stringResource(R.string.search_charts)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        if (state.loading) { LinearProgressIndicator(Modifier.fillMaxWidth()); StatusMessage(R.string.loading) }
        if (state.error) { StatusMessage(R.string.storage_error); SecondaryButton(R.string.retry, onRetry) }
        val shown = state.charts.filter { it.brand.contains(state.query, true) || it.title.contains(state.query, true) }
        if (!state.loading && shown.isEmpty()) {
            StatusMessage(if (state.charts.isEmpty()) R.string.no_charts else R.string.no_search_results)
            if (state.query.isNotEmpty()) SecondaryButton(R.string.clear_search, { onQuery("") })
        }
        if (state.charts.isNotEmpty() && state.charts.sumOf { it.rows.size } > 0) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(R.string.charts_count, state.charts.size, Modifier.weight(1f))
            StatCard(R.string.size_rows, state.charts.sumOf { it.rows.size }, Modifier.weight(1f))
        }
        shown.forEach { chart ->
            NotebookCard {
                Text(stringResource(R.string.data_text, chart.brand), style = MaterialTheme.typography.labelSmall)
                Text(stringResource(R.string.data_text, chart.title), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(chart.basis.label), style = MaterialTheme.typography.labelSmall)
                Text(stringResource(chart.category.label), style = MaterialTheme.typography.labelSmall)
                if (chart.isSample) Text(stringResource(R.string.sample_badge), style = MaterialTheme.typography.labelSmall)
                Text(stringResource(R.string.row_count, chart.rows.size))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(R.string.open, { onNavigate(Screen.ChartEditor(chart.id)) }, Modifier.weight(1f))
                    SecondaryButton(R.string.compare_action, { onNavigate(Screen.Compare(chartId = chart.id)) }, Modifier.weight(1f))
                }
                SecondaryButton(R.string.delete, { onAskDelete(chart.id) }, enabled = !state.busy)
            }
        }
    }
    state.charts.find { it.id == state.deleteId }?.let {
        ConfirmDeleteDialog(it.title, R.string.delete_chart_detail, onDelete, { onAskDelete(null) })
    }
}
