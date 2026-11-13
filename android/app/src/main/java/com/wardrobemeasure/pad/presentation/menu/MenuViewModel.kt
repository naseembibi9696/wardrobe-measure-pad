package com.wardrobemeasure.pad.presentation.menu

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

data class MenuUiState(val loading: Boolean = true, val error: Boolean = false, val recovered: Boolean = false,
    val profiles: List<Profile> = emptyList(), val selectedId: String? = null,
    val charts: Int = 0, val records: List<MeasurementRecord> = emptyList(), val drafts: List<MeasurementDraft> = emptyList(), val selectionError: Boolean = false)

class MenuViewModel(private val notebook: NotebookRepository) : EventViewModel<MenuUiState>() {
    private val mutableState = MutableStateFlow(MenuUiState())
    override val uiState = mutableState.asStateFlow()
    init { viewModelScope.launch { notebook.state.collect { state ->
        val data = state.notebook
        mutableState.update { it.copy(loading = state.loading, error = state.error, recovered = state.recovered,
            profiles = data.profiles, selectedId = data.settings.selectedProfileId?.takeIf { id -> data.profiles.any { it.id == id } } ?: data.profiles.firstOrNull()?.id,
            charts = data.charts.size, records = data.records, drafts = data.drafts) }
    } } }
    fun retry() { viewModelScope.launch { notebook.load() } }
    fun select(id: String) {
        if (id == uiState.value.selectedId) return
        viewModelScope.launch {
        val saved = notebook.change { it.copy(settings = it.settings.copy(selectedProfileId = id)) }
        mutableState.update { it.copy(selectionError = !saved) }
        if (saved && notebook.state.value.notebook.settings.haptics) emit(UiEvent.Haptic)
    } }
    fun play(id: String? = uiState.value.selectedId) {
        if (id == null) return
        viewModelScope.launch {
            val saved = id == notebook.state.value.notebook.settings.selectedProfileId || notebook.change { it.copy(settings = it.settings.copy(selectedProfileId = id)) }
            mutableState.update { it.copy(selectionError = !saved) }
            if (saved) emit(UiEvent.Navigate(Screen.Game(id)))
        }
    }
    fun navigate(screen: Screen) { emit(UiEvent.Navigate(screen)) }
}
