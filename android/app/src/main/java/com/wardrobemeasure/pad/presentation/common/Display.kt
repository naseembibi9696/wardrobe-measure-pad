package com.wardrobemeasure.pad.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.domain.model.*
import java.text.DateFormat
import java.util.Date

val FieldKey.label: Int get() = when (this) {
    FieldKey.CHEST -> R.string.chest; FieldKey.WAIST -> R.string.waist; FieldKey.HIPS -> R.string.hips
    FieldKey.INSEAM -> R.string.inseam; FieldKey.HEIGHT -> R.string.height
    FieldKey.SHOULDER -> R.string.shoulder; FieldKey.SLEEVE -> R.string.sleeve; FieldKey.NECK -> R.string.neck
}
val Category.label: Int get() = when (this) {
    Category.TOP -> R.string.tops; Category.BOTTOM -> R.string.bottoms; Category.FULL_BODY -> R.string.full_body; Category.NECKWEAR -> R.string.neckwear
}
val ChartBasis.label: Int get() = if (this == ChartBasis.BODY) R.string.body_ranges else R.string.garment_ranges
val UnitSystem.label: Int get() = if (this == UnitSystem.CM) R.string.cm else R.string.inches
@Composable
fun lengthText(value: Long?, unit: UnitSystem): String = if (value == null) stringResource(R.string.not_recorded)
    else stringResource(R.string.length_value, MeasurementUnits.format(value, unit), stringResource(unit.label))
fun dateText(timestamp: Long): String = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(timestamp))
