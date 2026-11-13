package com.wardrobemeasure.pad.presentation.charteditor

import kotlinx.coroutines.flow.collect

import androidx.lifecycle.viewModelScope
import com.wardrobemeasure.pad.core.navigation.*
import com.wardrobemeasure.pad.domain.model.*
import com.wardrobemeasure.pad.domain.repository.NotebookRepository
import com.wardrobemeasure.pad.domain.usecase.ValidateChartUseCase
import com.wardrobemeasure.pad.presentation.common.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RangeDraft(val min: Long? = null, val max: Long? = null)
data class RowDraft(val id: String = newId(), val label: String = "", val ranges: Map<FieldKey, RangeDraft> = emptyMap())
data class ChartEditorUiState(val loading: Boolean = true, val editing: Boolean = false, val brand: String = "", val title: String = "",
    val basis: ChartBasis = ChartBasis.BODY, val category: Category = Category.TOP, val keys: List<FieldKey> = listOf(FieldKey.CHEST),
    val rows: List<RowDraft> = listOf(RowDraft()), val unit: UnitSystem = UnitSystem.CM, val picker: PickerState? = null,
    val dirty: Boolean = false, val busy: Boolean = false, val invalid: Boolean = false, val error: Boolean = false,
    val confirmBack: Boolean = false, val removeRowId: String? = null, val invalidRow: String? = null, val invalidKey: FieldKey? = null)

class ChartEditorViewModel(private val notebook: NotebookRepository, private val route: Screen.ChartEditor) : EventViewModel<ChartEditorUiState>() {
    private val mutableState = MutableStateFlow(ChartEditorUiState(editing = route.chartId != null, basis = route.basis))
    override val uiState = mutableState.asStateFlow()
    private var bound = false
    private var original: SizeChart? = null
    private var active = true
    init { viewModelScope.launch { notebook.state.collect { data ->
        mutableState.update { it.copy(unit = data.notebook.settings.unit) }
        if (!data.loading && !bound) {
            original = data.notebook.charts.find { it.id == route.chartId }
            original?.let { chart -> mutableState.update { it.copy(brand = chart.brand, title = chart.title, basis = chart.basis,
                category = chart.category, keys = chart.keys, rows = chart.rows.map { row -> RowDraft(row.id, row.label, row.ranges.mapValues { RangeDraft(it.value.min, it.value.max) }) }) } }
            mutableState.update { it.copy(loading = false) }; bound = true
        }
    } } }
    fun resume() { active = true }
    fun pause() { active = false }
    private fun edit(change: (ChartEditorUiState) -> ChartEditorUiState) {
        if (!uiState.value.busy) mutableState.update { change(it).copy(dirty = true, invalid = false, invalidRow = null, invalidKey = null) }
    }
    fun brand(text: String) = edit { it.copy(brand = text) }
    fun title(text: String) = edit { it.copy(title = text) }
    fun basis(value: ChartBasis) = edit { state ->
        val category = if (value == ChartBasis.FINISHED_GARMENT && state.category == Category.NECKWEAR) Category.TOP else state.category
        state.copy(basis = value, category = category, keys = state.keys.filter { it in chartFields(category, value) })
    }
    fun category(value: Category) = edit { it.copy(category = value, keys = it.keys.filter { key -> key in chartFields(value, it.basis) }) }
    fun toggle(key: FieldKey) = edit { it.copy(keys = if (key in it.keys) it.keys - key else it.keys + key) }
    fun rowLabel(id: String, text: String) = edit { it.copy(rows = it.rows.map { row -> if (row.id == id) row.copy(label = text) else row }) }
    fun addRow() = edit { if (it.rows.size < 12) it.copy(rows = it.rows + RowDraft()) else it }
    fun move(id: String, direction: Int) = edit { state ->
        val index = state.rows.indexOfFirst { it.id == id }
        val next = index + direction
        if (index < 0 || next !in state.rows.indices) state else state.copy(rows = state.rows.toMutableList().apply { add(next, removeAt(index)) })
    }
    fun askRemove(id: String) {
        val row = uiState.value.rows.find { it.id == id } ?: return
        if (uiState.value.rows.size <= 1) return
        if (row.label.isNotBlank() || row.ranges.isNotEmpty()) mutableState.update { it.copy(removeRowId = id) } else remove(id)
    }
    fun cancelRemove() { mutableState.update { it.copy(removeRowId = null) } }
    fun remove(id: String? = uiState.value.removeRowId) { if (id != null) edit { it.copy(rows = it.rows.filterNot { row -> row.id == id }, removeRowId = null) } }
    fun openRange(id: String, key: FieldKey, maximum: Boolean) {
        val state = uiState.value
        val range = state.rows.find { it.id == id }?.ranges?.get(key)
        mutableState.update { it.copy(picker = pickerFor(key, state.unit, if (maximum) range?.max else range?.min, rowId = id, maximum = maximum)) }
    }
    fun pickerText(text: String) { mutableState.update { it.copy(picker = it.picker?.change(text)) } }
    fun pickerStep(direction: Int) { mutableState.update { it.copy(picker = it.picker?.step(direction)) } }
    fun closePicker() { mutableState.update { it.copy(picker = null) } }
    fun confirmPicker() {
        val picker = uiState.value.picker ?: return
        val value = picker.value()
        if (value == null || !MeasurementUnits.valid(value)) { mutableState.update { it.copy(picker = picker.copy(error = true)) }; return }
        edit { state -> state.copy(picker = null, rows = state.rows.map { row ->
            if (row.id != picker.rowId) row else {
                val old = row.ranges[picker.field] ?: RangeDraft()
                row.copy(ranges = row.ranges + (picker.field to if (picker.maximum) old.copy(max = value) else old.copy(min = value)))
            }
        }) }
    }
    fun back() {
        val state = uiState.value
        when { state.busy -> return; state.picker != null -> closePicker(); state.removeRowId != null -> cancelRemove()
            state.dirty -> mutableState.update { it.copy(confirmBack = true) }; else -> emit(UiEvent.Back) }
    }
    fun cancelBack() { mutableState.update { it.copy(confirmBack = false) } }
    fun discard() { emit(UiEvent.Back) }
    fun save() {
        val state = uiState.value
        if (state.busy) return
        val bad = state.rows.firstNotNullOfOrNull { row -> state.keys.firstOrNull { key ->
            val range = row.ranges[key]
            range?.min == null || range.max == null || range.min > range.max
        }?.let { row.id to it } }
        val labels = mutableSetOf<String>()
        val badLabel = state.rows.firstOrNull { row -> row.label.trim().length !in 1..12 || !labels.add(MeasurementUnits.normalizedLabel(row.label)) }
        val rows = state.rows.map { row -> ChartRow(row.id, row.label.trim(), row.ranges.mapNotNull { (key, range) ->
            val min = range.min; val max = range.max
            if (key in state.keys && min != null && max != null) key to LengthRange(min, max) else null
        }.toMap()) }
        val chart = SizeChart(original?.id ?: newId(), state.brand.trim(), state.title.trim(), state.basis, state.category, state.keys.toList(), rows,
            original?.isSample ?: false)
        if (!ValidateChartUseCase()(chart)) {
            mutableState.update { it.copy(invalid = true, invalidRow = badLabel?.id ?: bad?.first,
                invalidKey = if (badLabel != null) null else bad?.second, confirmBack = false) }; return
        }
        mutableState.update { it.copy(busy = true, error = false, confirmBack = false) }
        viewModelScope.launch {
            val saved = notebook.change { it.copy(charts = it.charts.filterNot { c -> c.id == chart.id } + chart) }
            mutableState.update { it.copy(busy = false, error = !saved, dirty = if (saved) false else it.dirty) }
            if (saved && active) {
                if (notebook.state.value.notebook.settings.haptics) emit(UiEvent.Haptic)
                emit(UiEvent.ChartSaved(chart.id, route.fromResult))
            }
        }
    }
}
