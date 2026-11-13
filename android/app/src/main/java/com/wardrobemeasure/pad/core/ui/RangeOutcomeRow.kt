package com.wardrobemeasure.pad.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import com.wardrobemeasure.pad.R

@Composable
fun RangeOutcomeRow(label: String, source: String, range: String, outcome: String) {
    NotebookCard(modifier = Modifier.semantics { stateDescription = outcome }) {
        Text(stringResource(R.string.data_text, label), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.source_value, source))
        Text(stringResource(R.string.chart_range, range))
        Text(stringResource(R.string.data_text, outcome), style = MaterialTheme.typography.labelLarge)
    }
}
