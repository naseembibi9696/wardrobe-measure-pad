package com.wardrobemeasure.pad.presentation.guide

import com.wardrobemeasure.pad.core.navigation.*
import com.wardrobemeasure.pad.domain.model.*
import com.wardrobemeasure.pad.presentation.common.EventViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MeasureGuideUiState(val field: FieldKey, val mode: MeasureMode, val step: Int = 0, val stepCount: Int = 3)

class MeasureGuideViewModel(route: Screen.MeasureGuide) : EventViewModel<MeasureGuideUiState>() {
    private val mutableState = MutableStateFlow(MeasureGuideUiState(route.field, route.mode))
    override val uiState = mutableState.asStateFlow()
    fun step(direction: Int) { mutableState.update { it.copy(step = (it.step + direction).coerceIn(0, 2)) } }
    fun back() { emit(UiEvent.Back) }
    fun use() { emit(UiEvent.UseMeasurement(uiState.value.field)) }
}
