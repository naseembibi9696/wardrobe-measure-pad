package com.wardrobemeasure.pad.data.sample

import com.wardrobemeasure.pad.domain.model.*

object SampleData {
    private fun cm(value: Int) = value * 10000L
    private fun values(vararg values: Int): Map<FieldKey, Long> = FieldKey.entries.zip(values.map { cm(it) }).toMap()
    fun notebook(): Notebook {
        val alex = Profile(id = "sample-alex", name = "Alex", relationship = "Self", isSample = true)
        val mia = Profile(id = "sample-mia", name = "Mia", relationship = "Family", avatar = 1, isSample = true)
        val shirt = Garment(id = "sample-linen-shirt", profileId = alex.id, name = "Blue linen shirt", isSample = true)
        fun row(label: String, keys: List<FieldKey>, vararg ends: Int) = ChartRow(label = label,
            ranges = keys.mapIndexed { index, key -> key to LengthRange(cm(ends[index * 2]), cm(ends[index * 2 + 1])) }.toMap())
        val bodyKeys = listOf(FieldKey.CHEST, FieldKey.WAIST, FieldKey.HIPS)
        val garmentKeys = listOf(FieldKey.CHEST, FieldKey.SHOULDER, FieldKey.SLEEVE)
        return Notebook(profiles = listOf(alex, mia), garments = listOf(shirt), records = listOf(
            MeasurementRecord(alex.id, alex.id, values = values(96, 80, 98, 80, 176, 44, 61, 38)),
            MeasurementRecord(mia.id, mia.id, values = values(64, 58, 68, 52, 122, 30, 43, 28)),
            MeasurementRecord(shirt.id, alex.id, shirt.id, mapOf(FieldKey.CHEST to cm(52), FieldKey.WAIST to cm(50), FieldKey.SHOULDER to cm(45), FieldKey.SLEEVE to cm(62)))
        ), charts = listOf(
            SizeChart(id = "sample-body-chart", brand = "Linen Studio", title = "Everyday tops", keys = bodyKeys, isSample = true,
                rows = listOf(row("S", bodyKeys, 88,93,72,77,90,95), row("M", bodyKeys,94,100,78,84,96,102), row("L", bodyKeys,101,108,85,92,103,110))),
            SizeChart(id = "sample-garment-chart", brand = "Linen Studio", title = "Linen shirt dimensions", basis = ChartBasis.FINISHED_GARMENT,
                keys = garmentKeys, isSample = true, rows = listOf(row("S", garmentKeys,96,100,42,44,59,61), row("M", garmentKeys,102,106,44,46,61,63), row("L", garmentKeys,108,112,46,48,63,65)))
        ), settings = NotebookSettings(selectedProfileId = alex.id))
    }
}
