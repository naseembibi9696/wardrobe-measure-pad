package com.wardrobemeasure.pad.presentation.common

import com.wardrobemeasure.pad.domain.model.*

data class PickerState(val field: FieldKey, val unit: UnitSystem, val original: Long?, val text: String,
    val edited: Boolean = false, val error: Boolean = false, val flat: Boolean = false,
    val rowId: String? = null, val maximum: Boolean = false) {
    fun value(): Long? = if (!edited) original else MeasurementUnits.parse(text, unit)
    fun change(text: String): PickerState {
        val value = MeasurementUnits.parse(text, unit)
        return copy(text = text, edited = true, error = value == null || !MeasurementUnits.valid(value, flat))
    }
    fun step(direction: Int): PickerState {
        val value = value() ?: return this
        val next = value + direction * unit.step
        return if (MeasurementUnits.valid(next, flat)) copy(original = next, text = MeasurementUnits.format(next, unit), edited = false, error = false) else this
    }
}
fun pickerFor(field: FieldKey, unit: UnitSystem, value: Long?, flat: Boolean = false, rowId: String? = null, maximum: Boolean = false) =
    PickerState(field, unit, value, value?.let { MeasurementUnits.format(it, unit) } ?: "", flat = flat, rowId = rowId, maximum = maximum)
