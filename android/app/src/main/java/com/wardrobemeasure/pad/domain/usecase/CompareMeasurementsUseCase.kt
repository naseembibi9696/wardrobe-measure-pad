package com.wardrobemeasure.pad.domain.usecase

import com.wardrobemeasure.pad.domain.model.*
import java.math.BigDecimal
import java.math.MathContext

class CompareMeasurementsUseCase {
    operator fun invoke(profileId: String, garmentId: String?, sourceName: String, raw: Map<FieldKey, Long>, chart: SizeChart): SavedResult? {
        val mode = if (garmentId == null) MeasureMode.BODY else MeasureMode.GARMENT
        if ((mode == MeasureMode.BODY) != (chart.basis == ChartBasis.BODY)) return null
        if (chart.keys.isEmpty() || chart.keys.any { raw[it] == null } || chart.rows.isEmpty()) return null
        data class Ranked(val row: ChartRow, val index: Int, val outcomes: List<FieldOutcome>, val gaps: BigDecimal, val centres: BigDecimal)
        val ranked = chart.rows.mapIndexedNotNull { index, row ->
            if (chart.keys.any { row.ranges[it] == null }) return@mapIndexedNotNull null
            var gaps = BigDecimal.ZERO
            var centres = BigDecimal.ZERO
            val outcomes = chart.keys.map { key ->
                val original = raw.getValue(key)
                val value = canonicalValue(key, original, mode)
                val range = row.ranges.getValue(key)
                val gap = when { value < range.min -> range.min - value; value > range.max -> value - range.max; else -> 0L }
                val outcome = when { value < range.min -> Outcome.BELOW; value > range.max -> Outcome.ABOVE; else -> Outcome.WITHIN }
                val scale = BigDecimal.valueOf(maxOf(range.max - range.min, 10000L))
                gaps = gaps.add(BigDecimal.valueOf(gap).divide(scale, MathContext.DECIMAL128))
                val midpoint = BigDecimal.valueOf(range.min + range.max).divide(BigDecimal.valueOf(2L))
                centres = centres.add(BigDecimal.valueOf(value).subtract(midpoint).abs().divide(scale, MathContext.DECIMAL128))
                FieldOutcome(key, original, value, range, outcome, gap)
            }
            Ranked(row, index, outcomes, gaps, centres)
        }
        val best = ranked.minWithOrNull(compareBy<Ranked> { r -> r.outcomes.count { it.gap > 0 } }
            .thenBy { it.gaps }.thenBy { it.centres }.thenBy { it.index }) ?: return null
        return SavedResult(kind = ResultKind.COMPARISON, profileId = profileId, garmentId = garmentId,
            sourceName = sourceName, values = raw.toMap(), chartId = chart.id,
            chartTitle = chart.title, chartBrand = chart.brand, chartBasis = chart.basis, chartCategory = chart.category, size = best.row.label,
            matched = best.outcomes.all { it.gap == 0L }, outcomes = best.outcomes)
    }
}
