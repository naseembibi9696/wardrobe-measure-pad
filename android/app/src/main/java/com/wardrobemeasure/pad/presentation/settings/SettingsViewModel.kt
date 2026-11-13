package com.wardrobemeasure.pad.presentation.settings

import kotlinx.coroutines.flow.collect

import androidx.lifecycle.viewModelScope
import com.wardrobemeasure.pad.core.navigation.UiEvent
import com.wardrobemeasure.pad.domain.model.UnitSystem
import com.wardrobemeasure.pad.domain.repository.NotebookRepository
import com.wardrobemeasure.pad.presentation.common.EventViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(val unit: UnitSystem = UnitSystem.CM, val haptics: Boolean = true,
    val busy: Boolean = false, val error: Boolean = false, val saved: Boolean = false)

class SettingsViewModel(private val notebook: NotebookRepository) : EventViewModel<SettingsUiState>() {
    private val mutableState = MutableStateFlow(SettingsUiState())
    override val uiState = mutableState.asStateFlow()
    private var retryUnit: UnitSystem? = null
    private var retryHaptics: Boolean? = null
    init { viewModelScope.launch { notebook.state.collect { data -> mutableState.update {
        it.copy(unit = data.notebook.settings.unit, haptics = data.notebook.settings.haptics)
    } } } }
    fun unit(value: UnitSystem) = save(value, null)
    fun haptics(value: Boolean) = save(null, value)
    fun retry() = save(retryUnit, retryHaptics)
    private fun save(unit: UnitSystem?, haptics: Boolean?) {
        if (uiState.value.busy) return
        retryUnit = unit; retryHaptics = haptics
        mutableState.update { it.copy(busy = true, saved = false, error = false) }
        viewModelScope.launch {
            val saved = notebook.change { it.copy(settings = it.settings.copy(unit = unit ?: it.settings.unit, haptics = haptics ?: it.settings.haptics)) }
            mutableState.update { it.copy(busy = false, error = !saved, saved = saved) }
        }
    }
    fun back() { emit(UiEvent.Back) }
    fun menu() { emit(UiEvent.Menu) }
}
