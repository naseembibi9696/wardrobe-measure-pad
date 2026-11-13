package com.wardrobemeasure.pad.domain.usecase

import com.wardrobemeasure.pad.domain.model.*

class ValidateChartUseCase {
    operator fun invoke(chart: SizeChart): Boolean {
        if (chart.brand.trim().length !in 1..40 || chart.title.trim().length !in 1..40) return false
        if (chart.keys.isEmpty() || !chartFields(chart.category, chart.basis).containsAll(chart.keys)) return false
        if (chart.rows.size !in 1..12) return false
        if (chart.rows.map { MeasurementUnits.normalizedLabel(it.label) }.distinct().size != chart.rows.size) return false
        return chart.rows.all { row -> row.label.trim().length in 1..12 && chart.keys.all { key ->
            row.ranges[key]?.let { MeasurementUnits.valid(it.min) && MeasurementUnits.valid(it.max) && it.min <= it.max } == true
        } }
    }
}
