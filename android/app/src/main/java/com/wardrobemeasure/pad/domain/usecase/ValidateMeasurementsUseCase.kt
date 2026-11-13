package com.wardrobemeasure.pad.domain.usecase

import com.wardrobemeasure.pad.domain.model.*

class ValidateMeasurementsUseCase {
    operator fun invoke(values: Map<FieldKey, Long>, mode: MeasureMode): Boolean =
        values.isNotEmpty() && values.all { (key, value) -> MeasurementUnits.valid(value, key.isFlatWidth(mode)) }
}
