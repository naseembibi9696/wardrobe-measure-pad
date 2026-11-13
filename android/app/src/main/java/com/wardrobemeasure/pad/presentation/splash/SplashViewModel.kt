package com.wardrobemeasure.pad.presentation.splash

import androidx.lifecycle.viewModelScope
import com.wardrobemeasure.pad.core.config.GameConfig
import com.wardrobemeasure.pad.core.navigation.UiEvent
import com.wardrobemeasure.pad.presentation.common.EventViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SplashUiState(val finished: Boolean = false)

class SplashViewModel : EventViewModel<SplashUiState>() {
    private val mutableState = MutableStateFlow(SplashUiState())
    override val uiState = mutableState.asStateFlow()
    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()
    init {
        viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            _finished.value = true
            mutableState.update { it.copy(finished = true) }
            emit(UiEvent.Finished)
        }
    }
}
