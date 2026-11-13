package com.wardrobemeasure.pad.presentation.game

import kotlinx.coroutines.flow.collect

import androidx.lifecycle.viewModelScope
import com.wardrobemeasure.pad.core.navigation.*
import com.wardrobemeasure.pad.domain.model.*
import com.wardrobemeasure.pad.domain.repository.NotebookRepository
import com.wardrobemeasure.pad.domain.usecase.ValidateMeasurementsUseCase
import com.wardrobemeasure.pad.presentation.common.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GarmentEditorState(val id: String? = null, val name: String = "", val category: Category = Category.TOP, val error: Boolean = false, val confirmPrune: Boolean = false)
enum class PendingAction { BACK, COMPARE }
data class GameUiState(
    val loading: Boolean = true, val profileId: String = "", val profileName: String = "",
    val mode: MeasureMode = MeasureMode.BODY, val garmentId: String? = null,
    val garments: List<Garment> = emptyList(), val fields: List<FieldKey> = FieldKey.entries,
    val values: Map<FieldKey, Long> = emptyMap(), val unit: UnitSystem = UnitSystem.CM,
    val picker: PickerState? = null, val dirty: Boolean = false, val draftSaved: Boolean = false,
    val busy: Boolean = false, val error: Boolean = false, val invalid: Boolean = false,
    val pending: PendingAction? = null, val garmentEditor: GarmentEditorState? = null, val deleteGarment: Boolean = false,
    val unitPicker: Boolean = false, val missingSource: Boolean = false
)

class GameViewModel(private val notebook: NotebookRepository, private val route: Screen.Game) : EventViewModel<GameUiState>() {
    private val mutableState = MutableStateFlow(GameUiState(profileId = route.profileId, mode = route.mode, garmentId = route.garmentId))
    override val uiState = mutableState.asStateFlow()
    private var boundSource: String? = null
    private var active = true
    private var draftJob: Job? = null
    private var pendingDrafts: Map<String, MeasurementDraft> = emptyMap()
    private var sessionDrafts: Map<String, MeasurementDraft> = emptyMap()
    private val validate = ValidateMeasurementsUseCase()
    init { viewModelScope.launch { notebook.state.collect { data ->
        val book = data.notebook
        val profile = book.profiles.find { it.id == route.profileId }
        mutableState.update { state -> state.copy(loading = data.loading, missingSource = !data.loading && profile == null,
            profileName = profile?.name.orEmpty(), garments = book.garments.filter { it.profileId == route.profileId }, unit = book.settings.unit) }
        if (!data.loading && profile != null && boundSource == null) bind(uiState.value.mode, route.garmentId)
    } } }

    fun resume() {
        active = true
        val state = uiState.value
        if (!state.loading && !state.dirty && state.picker == null && boundSource != null) bind(state.mode, state.garmentId, true)
    }
    fun pause() { active = false }
    private fun source(): String = sourceId(route.profileId, uiState.value.garmentId)
    fun bind(mode: MeasureMode, garmentId: String? = null, force: Boolean = false) {
        if (uiState.value.busy) return
        val book = notebook.state.value.notebook
        val garment = if (mode == MeasureMode.GARMENT) book.garments.find { it.id == garmentId && it.profileId == route.profileId }
            ?: book.garments.firstOrNull { it.profileId == route.profileId } else null
        val id = sourceId(route.profileId, garment?.id)
        if (!force && boundSource == id && uiState.value.mode == mode && uiState.value.garmentId == garment?.id) return
        val record = if (mode == MeasureMode.GARMENT && garment == null) null else book.records.find { it.sourceId == id }
        val draft = if (mode == MeasureMode.GARMENT && garment == null) null else sessionDrafts[id] ?: book.drafts.find { it.sourceId == id }
        boundSource = id
        mutableState.update { it.copy(mode = mode, garmentId = garment?.id, fields = fieldsFor(mode, garment?.category ?: Category.TOP),
            values = draft?.values ?: record?.values ?: emptyMap(), dirty = draft != null, draftSaved = draft != null,
            error = false, invalid = false, picker = null) }
    }
    fun openPicker(key: FieldKey) { if (!uiState.value.busy) mutableState.update { it.copy(picker = pickerFor(key, it.unit, it.values[key], key.isFlatWidth(it.mode))) } }
    fun pickerText(text: String) { mutableState.update { it.copy(picker = it.picker?.change(text)) } }
    fun pickerStep(direction: Int) { mutableState.update { it.copy(picker = it.picker?.step(direction)) } }
    fun closePicker() { mutableState.update { it.copy(picker = null) } }
    fun confirmPicker() {
        val picker = uiState.value.picker ?: return
        val value = picker.value()
        if (value == null || !MeasurementUnits.valid(value, picker.flat)) {
            mutableState.update { it.copy(picker = picker.copy(error = true)) }; return
        }
        closePicker()
        if (value != uiState.value.values[picker.field]) edit(picker.field, value)
    }
    fun step(key: FieldKey, direction: Int) {
        val state = uiState.value
        val value = state.values[key] ?: run { openPicker(key); return }
        val next = value + direction * state.unit.step
        if (MeasurementUnits.valid(next, key.isFlatWidth(state.mode))) edit(key, next)
    }
    fun clear(key: FieldKey) { edit(key, null) }
    private fun edit(key: FieldKey, value: Long?) {
        if (uiState.value.busy) return
        val next = uiState.value.values.toMutableMap().apply { if (value == null) remove(key) else put(key, value) }.toMap()
        mutableState.update { it.copy(values = next, dirty = true, draftSaved = false, error = false, invalid = false) }
        queueDraft(MeasurementDraft(source(), next, notebook.state.value.notebook.records.find { it.sourceId == source() }?.updatedAt ?: 0L))
    }
    private fun queueDraft(draft: MeasurementDraft) {
        sessionDrafts = sessionDrafts + (draft.sourceId to draft)
        pendingDrafts = pendingDrafts + (draft.sourceId to draft)
        if (draftJob?.isActive == true) return
        draftJob = viewModelScope.launch {
            while (pendingDrafts.isNotEmpty()) {
                val entry = pendingDrafts.entries.first()
                pendingDrafts = pendingDrafts - entry.key
                val success = notebook.change { it.copy(drafts = it.drafts.filterNot { d -> d.sourceId == entry.key } + entry.value) }
                if (entry.key == source() && pendingDrafts[entry.key] == null) mutableState.update { it.copy(draftSaved = success, error = !success) }
            }
        }
    }
    fun retryDraft() { queueDraft(MeasurementDraft(source(), uiState.value.values)) }
    fun guide(key: FieldKey) { emit(UiEvent.Navigate(Screen.MeasureGuide(route.id, key, uiState.value.mode))) }
    fun save(continueToCompare: Boolean = false) {
        val state = uiState.value
        if (state.busy) return
        if (!validate(state.values, state.mode)) { mutableState.update { it.copy(invalid = true) }; return }
        mutableState.update { it.copy(busy = true, error = false, pending = null) }
        viewModelScope.launch {
            draftJob?.join()
            val record = MeasurementRecord(sourceId(state.profileId, state.garmentId), state.profileId, state.garmentId, state.values.toMap())
            val garment = state.garments.find { it.id == state.garmentId }
            val result = SavedResult(kind = ResultKind.SAVED, profileId = state.profileId, garmentId = state.garmentId,
                sourceName = garment?.name ?: state.profileName, values = state.values.toMap())
            val saved = notebook.change { it.copy(records = it.records.filterNot { r -> r.sourceId == record.sourceId } + record,
                drafts = it.drafts.filterNot { d -> d.sourceId == record.sourceId }, result = result,
                profiles = it.profiles.map { p -> if (p.id == state.profileId) p.copy(updatedAt = record.updatedAt) else p }) }
            if (saved) sessionDrafts = sessionDrafts - record.sourceId
            mutableState.update { it.copy(busy = false, error = !saved, dirty = if (saved) false else it.dirty, draftSaved = false) }
            if (saved && active) {
                if (continueToCompare) {
                    if (notebook.state.value.notebook.settings.haptics) emit(UiEvent.Haptic)
                    emit(UiEvent.Navigate(Screen.Compare(state.profileId, state.garmentId)))
                }
                else emit(UiEvent.Result(result.id, notebook.state.value.notebook.settings.haptics))
            }
        }
    }
    fun compare() { if (uiState.value.dirty) mutableState.update { it.copy(pending = PendingAction.COMPARE) }
        else emit(UiEvent.Navigate(Screen.Compare(route.profileId, uiState.value.garmentId))) }
    fun back() {
        val state = uiState.value
        when {
            state.busy -> return
            state.picker != null -> closePicker()
            state.garmentEditor != null -> cancelGarment()
            state.deleteGarment -> mutableState.update { it.copy(deleteGarment = false) }
            state.unitPicker -> mutableState.update { it.copy(unitPicker = false) }
            state.dirty -> mutableState.update { it.copy(pending = PendingAction.BACK) }
            else -> emit(UiEvent.Back)
        }
    }
    fun cancelPending() { mutableState.update { it.copy(pending = null) } }
    fun savePending() { save(uiState.value.pending == PendingAction.COMPARE) }
    fun discard() {
        val action = uiState.value.pending
        mutableState.update { it.copy(busy = true, pending = null) }
        viewModelScope.launch {
            draftJob?.join()
            val saved = notebook.change { it.copy(drafts = it.drafts.filterNot { d -> d.sourceId == source() }) }
            mutableState.update { it.copy(busy = false, error = !saved) }
            if (saved) {
                sessionDrafts = sessionDrafts - source()
                bind(uiState.value.mode, uiState.value.garmentId, true)
                if (active) emit(if (action == PendingAction.COMPARE) UiEvent.Navigate(Screen.Compare(route.profileId, uiState.value.garmentId)) else UiEvent.Back)
            }
        }
    }
    fun showUnits(show: Boolean) { mutableState.update { it.copy(unitPicker = show) } }
    fun setUnit(unit: UnitSystem) { viewModelScope.launch {
        val saved = notebook.change { it.copy(settings = it.settings.copy(unit = unit)) }
        mutableState.update { it.copy(unitPicker = !saved, error = !saved) }
    } }
    fun editGarment(existing: Boolean) {
        val garment = uiState.value.garments.find { it.id == uiState.value.garmentId }
        mutableState.update { it.copy(garmentEditor = if (existing && garment != null) GarmentEditorState(garment.id, garment.name, garment.category) else GarmentEditorState()) }
    }
    fun garmentName(name: String) { mutableState.update { it.copy(garmentEditor = it.garmentEditor?.copy(name = name, error = false)) } }
    fun garmentCategory(category: Category) { mutableState.update { it.copy(garmentEditor = it.garmentEditor?.copy(category = category, confirmPrune = false)) } }
    fun cancelGarment() { mutableState.update { it.copy(garmentEditor = null) } }
    fun saveGarment(confirmed: Boolean = false) {
        val editor = uiState.value.garmentEditor ?: return
        if (editor.name.trim().length !in 1..40) { mutableState.update { it.copy(garmentEditor = editor.copy(error = true)) }; return }
        val allowed = fieldsFor(MeasureMode.GARMENT, editor.category)
        val book = notebook.state.value.notebook
        val old = book.garments.find { it.id == editor.id }
        if (!confirmed && old != null && old.category != editor.category &&
            (uiState.value.values.keys.any { it !in allowed } || book.records.find { it.sourceId == old.id }?.values?.keys?.any { it !in allowed } == true)) {
            mutableState.update { it.copy(garmentEditor = editor.copy(confirmPrune = true)) }; return
        }
        mutableState.update { it.copy(busy = true) }
        viewModelScope.launch {
            draftJob?.join()
            val garment = Garment(editor.id ?: newId(), route.profileId, editor.name.trim(), editor.category, old?.isSample ?: false)
            val saved = notebook.change { it.copy(garments = it.garments.filterNot { g -> g.id == garment.id } + garment,
                records = it.records.map { r -> if (r.sourceId == garment.id) r.copy(values = r.values.filterKeys { k -> k in allowed }) else r },
                drafts = it.drafts.map { d -> if (d.sourceId == garment.id) d.copy(values = d.values.filterKeys { k -> k in allowed }) else d }) }
            mutableState.update { it.copy(busy = false, error = !saved, garmentEditor = if (saved) null else editor) }
            if (saved) {
                sessionDrafts[garment.id]?.let { draft -> sessionDrafts = sessionDrafts + (garment.id to draft.copy(values = draft.values.filterKeys { it in allowed })) }
                bind(MeasureMode.GARMENT, garment.id, true)
            }
        }
    }
    fun askDeleteGarment(show: Boolean) { mutableState.update { it.copy(deleteGarment = show) } }
    fun deleteGarment() {
        val id = uiState.value.garmentId ?: return
        mutableState.update { it.copy(busy = true) }
        viewModelScope.launch {
            draftJob?.join()
            val saved = notebook.change { it.copy(garments = it.garments.filterNot { g -> g.id == id }, records = it.records.filterNot { r -> r.sourceId == id }, drafts = it.drafts.filterNot { d -> d.sourceId == id }) }
            mutableState.update { it.copy(busy = false, error = !saved, deleteGarment = false) }
            if (saved) { sessionDrafts = sessionDrafts - id; bind(MeasureMode.GARMENT, force = true) }
        }
    }
}
