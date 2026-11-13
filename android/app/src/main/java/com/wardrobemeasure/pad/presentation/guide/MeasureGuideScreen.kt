package com.wardrobemeasure.pad.presentation.guide

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.ui.*
import com.wardrobemeasure.pad.domain.model.*
import com.wardrobemeasure.pad.presentation.common.label

private fun instructions(field: FieldKey, mode: MeasureMode): List<Int> {
    if (field.isFlatWidth(mode)) return listOf(R.string.guide_flat_1, R.string.guide_flat_2, R.string.guide_flat_3)
    if (mode == MeasureMode.GARMENT) return when (field) {
        FieldKey.INSEAM -> listOf(R.string.guide_garment_inseam_1, R.string.guide_garment_inseam_2, R.string.guide_garment_inseam_3)
        FieldKey.SHOULDER -> listOf(R.string.guide_garment_shoulder_1, R.string.guide_garment_shoulder_2, R.string.guide_garment_shoulder_3)
        FieldKey.SLEEVE -> listOf(R.string.guide_garment_sleeve_1, R.string.guide_garment_sleeve_2, R.string.guide_garment_sleeve_3)
        else -> listOf(R.string.guide_flat_1, R.string.guide_flat_2, R.string.guide_flat_3)
    }
    return when (field) {
        FieldKey.CHEST -> listOf(R.string.guide_chest_1, R.string.guide_chest_2, R.string.guide_chest_3)
        FieldKey.WAIST -> listOf(R.string.guide_waist_1, R.string.guide_waist_2, R.string.guide_waist_3)
        FieldKey.HIPS -> listOf(R.string.guide_hips_1, R.string.guide_hips_2, R.string.guide_hips_3)
        FieldKey.INSEAM -> listOf(R.string.guide_inseam_1, R.string.guide_inseam_2, R.string.guide_inseam_3)
        FieldKey.HEIGHT -> listOf(R.string.guide_height_1, R.string.guide_height_2, R.string.guide_height_3)
        FieldKey.SHOULDER -> listOf(R.string.guide_shoulder_1, R.string.guide_shoulder_2, R.string.guide_shoulder_3)
        FieldKey.SLEEVE -> listOf(R.string.guide_sleeve_1, R.string.guide_sleeve_2, R.string.guide_sleeve_3)
        FieldKey.NECK -> listOf(R.string.guide_neck_1, R.string.guide_neck_2, R.string.guide_neck_3)
    }
}

@Composable
fun MeasureGuideScreen(state: MeasureGuideUiState, onBack: () -> Unit, onStep: (Int) -> Unit, onUse: () -> Unit) {
    val field = stringResource(state.field.label)
    NotebookPage(R.string.measure_guide, onBack, bottom = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(R.string.previous, { onStep(-1) }, Modifier.weight(1f), state.step > 0)
            SecondaryButton(R.string.next, { onStep(1) }, Modifier.weight(1f), state.step < 2)
        }
        PrimaryButton(R.string.use_measurement, onUse)
    }) {
        Text(stringResource(R.string.data_text, field), style = MaterialTheme.typography.titleLarge)
        val stepDescription = stringResource(R.string.step_of, state.step + 1, state.stepCount)
        Text(stepDescription, modifier = Modifier.semantics { stateDescription = stepDescription; liveRegion = LiveRegionMode.Polite })
        val symbols = listOf(R.drawable.sprite_1, R.drawable.sprite_2, R.drawable.sprite_3, R.drawable.sprite_4, R.drawable.sprite_5)
        MeasurementGuideFigure(symbols[state.field.ordinal % symbols.size], stringResource(R.string.guide_description, field))
        instructions(state.field, state.mode).forEachIndexed { index, instruction ->
            val selection = stringResource(if (state.step == index) R.string.selected else R.string.not_selected)
            NotebookCard(selected = state.step == index, modifier = Modifier.semantics { stateDescription = selection }) {
                Text(stringResource(R.string.step_of, index + 1, 3), style = MaterialTheme.typography.labelSmall)
                Text(stringResource(instruction, field), style = MaterialTheme.typography.bodyLarge)
            }
        }
        Text(stringResource(R.string.guide_note), style = MaterialTheme.typography.bodyMedium)
    }
}
