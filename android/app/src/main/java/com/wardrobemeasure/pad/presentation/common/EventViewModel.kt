package com.wardrobemeasure.pad.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wardrobemeasure.pad.core.navigation.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class EventViewModel<S> : ViewModel() {
    abstract val uiState: StateFlow<S>
    private val eventChannel = Channel<UiEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()
    protected fun emit(event: UiEvent) { viewModelScope.launch { eventChannel.send(event) } }
}
