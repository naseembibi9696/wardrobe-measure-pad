package com.wardrobemeasure.pad.presentation.profileeditor

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

data class ProfileEditorUiState(val name: String = "", val relationship: String = "", val avatar: Int = 0,
    val editing: Boolean = false, val loading: Boolean = true, val dirty: Boolean = false,
    val invalid: Boolean = false, val busy: Boolean = false, val error: Boolean = false,
    val confirmDelete: Boolean = false, val confirmBack: Boolean = false)

class ProfileEditorViewModel(private val notebook: NotebookRepository, private val route: Screen.ProfileEditor) : EventViewModel<ProfileEditorUiState>() {
    private val mutableState = MutableStateFlow(ProfileEditorUiState(editing = route.profileId != null))
    override val uiState = mutableState.asStateFlow()
    private var original: Profile? = null
    private var bound = false
    private var active = true
    init { viewModelScope.launch { notebook.state.collect { data ->
        if (!bound && !data.loading) {
            original = data.notebook.profiles.find { it.id == route.profileId }
            mutableState.update { it.copy(name = original?.name.orEmpty(), relationship = original?.relationship.orEmpty(), avatar = original?.avatar ?: 0, loading = false) }
            bound = true
        }
    } } }
    fun resume() { active = true }
    fun pause() { active = false }
    fun name(value: String) { mutableState.update { it.copy(name = value, dirty = true, invalid = false) } }
    fun relationship(value: String) { mutableState.update { it.copy(relationship = value, dirty = true, invalid = false) } }
    fun avatar(value: Int) { mutableState.update { it.copy(avatar = value, dirty = true) } }
    fun back() { when { uiState.value.busy -> return
        uiState.value.confirmDelete -> confirmDelete(false)
        uiState.value.dirty -> mutableState.update { it.copy(confirmBack = true) }
        else -> emit(UiEvent.Back) } }
    fun cancelBack() { mutableState.update { it.copy(confirmBack = false) } }
    fun discard() { emit(UiEvent.Back) }
    fun confirmDelete(show: Boolean) { mutableState.update { it.copy(confirmDelete = show) } }
    fun save() {
        val state = uiState.value
        if (state.busy) return
        if (state.name.trim().length !in 1..30 || state.relationship.trim().length > 30) {
            mutableState.update { it.copy(invalid = true, confirmBack = false) }; return
        }
        mutableState.update { it.copy(busy = true, error = false, confirmBack = false) }
        viewModelScope.launch {
            val profile = Profile(original?.id ?: newId(), state.name.trim(), state.relationship.trim(), state.avatar, original?.isSample ?: false)
            val saved = notebook.change { it.copy(profiles = it.profiles.filterNot { p -> p.id == profile.id } + profile,
                settings = it.settings.copy(selectedProfileId = profile.id)) }
            mutableState.update { it.copy(busy = false, error = !saved, dirty = if (saved) false else it.dirty) }
            if (saved && active) {
                if (notebook.state.value.notebook.settings.haptics) emit(UiEvent.Haptic)
                emit(UiEvent.Menu)
            }
        }
    }
    fun delete() {
        val id = original?.id ?: return
        mutableState.update { it.copy(busy = true, confirmDelete = false) }
        viewModelScope.launch {
            val saved = notebook.change { book ->
                val garmentIds = book.garments.filter { it.profileId == id }.map { it.id }.toSet()
                val remaining = book.profiles.filterNot { it.id == id }
                book.copy(profiles = remaining, garments = book.garments.filterNot { it.profileId == id },
                    records = book.records.filterNot { it.profileId == id }, drafts = book.drafts.filterNot { it.sourceId == id || it.sourceId in garmentIds },
                    settings = book.settings.copy(selectedProfileId = if (book.settings.selectedProfileId == id) remaining.firstOrNull()?.id else book.settings.selectedProfileId),
                    result = book.result?.takeUnless { it.profileId == id })
            }
            mutableState.update { it.copy(busy = false, error = !saved) }
            if (saved && active) emit(UiEvent.Menu)
        }
    }
}
