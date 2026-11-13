package com.wardrobemeasure.pad.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormatSymbols
import java.util.Locale

object MeasurementUnits {
    const val MIN = 10000L
    const val MAX = 4000000L
    fun format(raw: Long, unit: UnitSystem): String = BigDecimal.valueOf(raw)
        .divide(BigDecimal.valueOf(unit.scale), 1, RoundingMode.HALF_UP).toPlainString()
        .replace('.', DecimalFormatSymbols.getInstance().decimalSeparator)
    fun parse(text: String, unit: UnitSystem): Long? {
        val normalized = text.trim().replace(DecimalFormatSymbols.getInstance().decimalSeparator, '.').replace(',', '.')
        if (!Regex("[0-9]+(?:\\.[0-9])?").matches(normalized)) return null
        return runCatching { BigDecimal(normalized).multiply(BigDecimal.valueOf(unit.scale)).longValueExact() }.getOrNull()
    }
    fun valid(raw: Long, flat: Boolean = false): Boolean = raw in MIN..MAX && (!flat || raw * 2 <= MAX)
    fun normalizedLabel(text: String): String = text.trim().lowercase(Locale.ROOT)
}
