package com.wardrobemeasure.pad.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wardrobemeasure.pad.core.navigation.Screen
import com.wardrobemeasure.pad.domain.repository.NotebookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(val screen: Screen = Screen.Splash, val backStack: List<Screen> = emptyList())

class AppViewModel(private val notebook: NotebookRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(AppUiState())
    val uiState = mutableState.asStateFlow()
    private val mutableScreen = MutableStateFlow<Screen>(Screen.Splash)
    val screen = mutableScreen.asStateFlow()

    init { viewModelScope.launch { notebook.load() } }
    private fun updateNavigation(state: AppUiState) { mutableState.update { state }; mutableScreen.value = state.screen }
    fun finishSplash() { if (screen.value == Screen.Splash) updateNavigation(AppUiState(Screen.Menu)) }
    fun navigate(next: Screen) {
        val current = uiState.value
        if (current.screen == next) return
        updateNavigation(AppUiState(next, if (current.screen == Screen.Splash) emptyList() else current.backStack + current.screen))
    }
    fun back() {
        val state = uiState.value
        val previous = state.backStack.lastOrNull() ?: Screen.Menu
        updateNavigation(AppUiState(previous, state.backStack.dropLast(1)))
    }
    fun menu() { updateNavigation(AppUiState(Screen.Menu)) }
    fun chartSaved(chartId: String, fromResult: Boolean) {
        if (!fromResult) { back(); return }
        val result = notebook.state.value.notebook.result
        val prior = uiState.value.backStack.indexOfLast { it is Screen.Compare }
        if (prior >= 0) {
            val stack = uiState.value.backStack
            val old = stack[prior] as Screen.Compare
            updateNavigation(AppUiState(old.copy(chartId = chartId), stack.take(prior)))
        } else navigate(Screen.Compare(result?.profileId, result?.garmentId, chartId))
    }
}
