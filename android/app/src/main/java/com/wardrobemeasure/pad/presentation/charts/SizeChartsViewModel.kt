package com.wardrobemeasure.pad.presentation.charts

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

data class SizeChartsUiState(val charts: List<SizeChart> = emptyList(), val query: String = "", val loading: Boolean = true,
    val error: Boolean = false, val deleteId: String? = null, val busy: Boolean = false)

class SizeChartsViewModel(private val notebook: NotebookRepository) : EventViewModel<SizeChartsUiState>() {
    private val mutableState = MutableStateFlow(SizeChartsUiState())
    override val uiState = mutableState.asStateFlow()
    init { viewModelScope.launch { notebook.state.collect { data ->
        mutableState.update { it.copy(charts = data.notebook.charts, loading = data.loading, error = data.error) }
    } } }
    fun query(value: String) { mutableState.update { it.copy(query = value) } }
    fun navigate(screen: Screen) { emit(UiEvent.Navigate(screen)) }
    fun back() { if (uiState.value.deleteId != null) askDelete(null) else emit(UiEvent.Back) }
    fun retry() { viewModelScope.launch { notebook.load() } }
    fun askDelete(id: String?) { mutableState.update { it.copy(deleteId = id) } }
    fun delete() {
        val id = uiState.value.deleteId ?: return
        mutableState.update { it.copy(busy = true) }
        viewModelScope.launch {
            val saved = notebook.change { it.copy(charts = it.charts.filterNot { c -> c.id == id }) }
            mutableState.update { it.copy(busy = false, error = !saved, deleteId = null) }
        }
    }
}
