package com.wardrobemeasure.pad.presentation.gameover

import kotlinx.coroutines.flow.collect

import androidx.lifecycle.viewModelScope
import com.wardrobemeasure.pad.core.navigation.*
import com.wardrobemeasure.pad.domain.model.*
import com.wardrobemeasure.pad.domain.repository.NotebookRepository
import com.wardrobemeasure.pad.presentation.common.EventViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameOverUiState(val loading: Boolean = true, val result: SavedResult? = null, val unit: UnitSystem = UnitSystem.CM, val chartExists: Boolean = false)

class GameOverViewModel(private val notebook: NotebookRepository, private val route: Screen.GameOver) : EventViewModel<GameOverUiState>() {
    private val mutableState = MutableStateFlow(GameOverUiState())
    override val uiState = mutableState.asStateFlow()
    init { viewModelScope.launch { notebook.state.collect { data ->
        mutableState.update { state ->
            val result = state.result ?: data.notebook.result?.takeIf { it.id == route.resultId }
            state.copy(loading = data.loading, result = result, unit = data.notebook.settings.unit, chartExists = data.notebook.charts.any { it.id == result?.chartId })
        }
    } } }
    fun again() { emit(UiEvent.Back) }
    fun menu() { emit(UiEvent.Menu) }
    fun compare() { uiState.value.result?.let { emit(UiEvent.Navigate(Screen.Compare(it.profileId, it.garmentId))) } }
    fun editChart() { uiState.value.result?.chartId?.let { emit(UiEvent.Navigate(Screen.ChartEditor(it, fromResult = true))) } }
}
