package com.wardrobemeasure.pad.presentation.compare

import kotlinx.coroutines.flow.collect

import androidx.lifecycle.viewModelScope
import com.wardrobemeasure.pad.core.navigation.*
import com.wardrobemeasure.pad.domain.model.*
import com.wardrobemeasure.pad.domain.repository.NotebookRepository
import com.wardrobemeasure.pad.domain.usecase.CompareMeasurementsUseCase
import com.wardrobemeasure.pad.presentation.common.EventViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompareUiState(val loading: Boolean = true, val profiles: List<Profile> = emptyList(), val profileId: String? = null,
    val mode: MeasureMode = MeasureMode.BODY, val garments: List<Garment> = emptyList(), val garmentId: String? = null,
    val charts: List<SizeChart> = emptyList(), val chartId: String? = null, val values: Map<FieldKey, Long> = emptyMap(),
    val keys: List<FieldKey> = emptyList(), val missing: List<FieldKey> = emptyList(), val unit: UnitSystem = UnitSystem.CM,
    val ready: Boolean = false, val busy: Boolean = false, val error: Boolean = false)

class CompareViewModel(private val notebook: NotebookRepository, private val route: Screen.Compare) : EventViewModel<CompareUiState>() {
    private val mutableState = MutableStateFlow(CompareUiState(profileId = route.profileId, garmentId = route.garmentId,
        mode = if (route.garmentId != null || notebook.state.value.notebook.charts.find { it.id == route.chartId }?.basis == ChartBasis.FINISHED_GARMENT) MeasureMode.GARMENT else MeasureMode.BODY, chartId = route.chartId))
    override val uiState = mutableState.asStateFlow()
    private var active = true
    private var lastRouteChartId = route.chartId
    init { viewModelScope.launch { notebook.state.collect { refresh() } } }
    fun resume() { active = true; refresh() }
    fun pause() { active = false }
    fun reselectChart(id: String?) {
        if (id != lastRouteChartId && id != null && notebook.state.value.notebook.charts.any { it.id == id }) mutableState.update { it.copy(chartId = id) }
        lastRouteChartId = id
        refresh()
    }
    private fun refresh() {
        val data = notebook.state.value
        val book = data.notebook
        val state = uiState.value
        val profile = book.profiles.find { it.id == state.profileId } ?: book.profiles.find { it.id == book.settings.selectedProfileId } ?: book.profiles.firstOrNull()
        val garments = book.garments.filter { it.profileId == profile?.id }
        val garment = if (state.mode == MeasureMode.GARMENT) garments.find { it.id == state.garmentId } ?: garments.firstOrNull() else null
        val basis = if (state.mode == MeasureMode.BODY) ChartBasis.BODY else ChartBasis.FINISHED_GARMENT
        val charts = book.charts.filter { it.basis == basis && (garment == null || it.category == garment.category) }
        val chart = charts.find { it.id == state.chartId } ?: charts.firstOrNull()
        val values = if (state.mode == MeasureMode.GARMENT && garment == null) emptyMap() else book.records.find { it.sourceId == sourceId(profile?.id.orEmpty(), garment?.id) }?.values ?: emptyMap()
        val missing = chart?.keys?.filter { values[it] == null } ?: emptyList()
        mutableState.update { it.copy(loading = data.loading, profiles = book.profiles, profileId = profile?.id, garments = garments,
            garmentId = garment?.id, charts = charts, chartId = chart?.id, values = values, keys = chart?.keys ?: emptyList(),
            missing = missing, unit = book.settings.unit, ready = !data.error && profile != null && chart != null && missing.isEmpty() && (state.mode == MeasureMode.BODY || garment != null)) }
    }
    fun selectProfile(id: String) { mutableState.update { it.copy(profileId = id, garmentId = null) }; refresh() }
    fun mode(mode: MeasureMode) { mutableState.update { it.copy(mode = mode, chartId = null) }; refresh() }
    fun garment(id: String) { mutableState.update { it.copy(garmentId = id) }; refresh() }
    fun chart(id: String) { mutableState.update { it.copy(chartId = id) }; refresh() }
    fun navigate(screen: Screen) { emit(UiEvent.Navigate(screen)) }
    fun back() { if (!uiState.value.busy) emit(UiEvent.Back) }
    fun updateMeasurements() { val id = uiState.value.profileId ?: return; navigate(Screen.Game(id, uiState.value.mode, uiState.value.garmentId)) }
    fun compare() {
        val state = uiState.value
        if (!state.ready || state.busy) return
        val chart = state.charts.find { it.id == state.chartId } ?: return
        val profile = state.profiles.find { it.id == state.profileId } ?: return
        val name = state.garments.find { it.id == state.garmentId }?.name ?: profile.name
        mutableState.update { it.copy(busy = true, error = false) }
        viewModelScope.launch {
            val result = CompareMeasurementsUseCase()(profile.id, state.garmentId, name, state.values, chart)
            val saved = result != null && notebook.change { it.copy(result = result) }
            mutableState.update { it.copy(busy = false, error = !saved) }
            if (saved && result != null && active) emit(UiEvent.Result(result.id, notebook.state.value.notebook.settings.haptics))
        }
    }
}
