package com.wardrobemeasure.pad.domain.model

import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()

enum class MeasureMode { BODY, GARMENT }
enum class UnitSystem(val scale: Long, val step: Long) { CM(10000L, 1000L), IN(25400L, 2540L) }
enum class FieldKey { CHEST, WAIST, HIPS, INSEAM, HEIGHT, SHOULDER, SLEEVE, NECK }
enum class Category { TOP, BOTTOM, FULL_BODY, NECKWEAR }
enum class ChartBasis { BODY, FINISHED_GARMENT }
enum class Outcome { WITHIN, BELOW, ABOVE }
enum class ResultKind { SAVED, COMPARISON }

data class Profile(
    val id: String = newId(), val name: String, val relationship: String = "",
    val avatar: Int = 0, val isSample: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
data class Garment(
    val id: String = newId(), val profileId: String, val name: String,
    val category: Category = Category.TOP, val isSample: Boolean = false
)
data class MeasurementRecord(
    val sourceId: String, val profileId: String, val garmentId: String? = null,
    val values: Map<FieldKey, Long> = emptyMap(), val updatedAt: Long = System.currentTimeMillis()
)
data class MeasurementDraft(
    val sourceId: String, val values: Map<FieldKey, Long>,
    val revision: Long = 0L, val confirmedAt: Long = System.currentTimeMillis()
)
data class LengthRange(val min: Long, val max: Long)
data class ChartRow(val id: String = newId(), val label: String, val ranges: Map<FieldKey, LengthRange>)
data class SizeChart(
    val id: String = newId(), val brand: String, val title: String,
    val basis: ChartBasis = ChartBasis.BODY, val category: Category = Category.TOP,
    val keys: List<FieldKey>, val rows: List<ChartRow>, val isSample: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
data class NotebookSettings(
    val unit: UnitSystem = UnitSystem.CM, val haptics: Boolean = true, val selectedProfileId: String? = null
)
data class FieldOutcome(
    val key: FieldKey, val raw: Long, val value: Long, val range: LengthRange,
    val outcome: Outcome, val gap: Long
)
data class SavedResult(
    val id: String = newId(), val kind: ResultKind, val profileId: String,
    val garmentId: String? = null, val sourceName: String, val values: Map<FieldKey, Long>,
    val chartId: String? = null, val chartTitle: String = "", val size: String = "",
    val matched: Boolean = false, val outcomes: List<FieldOutcome> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(), val chartBasis: ChartBasis? = null,
    val chartCategory: Category? = null, val chartBrand: String = ""
)
data class Notebook(
    val profiles: List<Profile> = emptyList(), val garments: List<Garment> = emptyList(),
    val records: List<MeasurementRecord> = emptyList(), val drafts: List<MeasurementDraft> = emptyList(),
    val charts: List<SizeChart> = emptyList(), val settings: NotebookSettings = NotebookSettings(),
    val result: SavedResult? = null, val seedCompleted: Boolean = true
)
data class NotebookState(
    val loading: Boolean = true, val notebook: Notebook = Notebook(),
    val error: Boolean = false, val recovered: Boolean = false
)

fun sourceId(profileId: String, garmentId: String?): String = garmentId ?: profileId
fun fieldsFor(mode: MeasureMode, category: Category = Category.TOP): List<FieldKey> =
    if (mode == MeasureMode.BODY) FieldKey.entries else when (category) {
        Category.TOP -> listOf(FieldKey.CHEST, FieldKey.WAIST, FieldKey.HIPS, FieldKey.SHOULDER, FieldKey.SLEEVE)
        Category.BOTTOM -> listOf(FieldKey.WAIST, FieldKey.HIPS, FieldKey.INSEAM)
        Category.FULL_BODY -> listOf(FieldKey.CHEST, FieldKey.WAIST, FieldKey.HIPS, FieldKey.INSEAM, FieldKey.SHOULDER, FieldKey.SLEEVE)
        Category.NECKWEAR -> listOf(FieldKey.NECK)
    }
fun chartFields(category: Category, basis: ChartBasis): List<FieldKey> {
    val keys = when (category) {
        Category.TOP -> fieldsFor(MeasureMode.GARMENT, Category.TOP)
        Category.BOTTOM -> fieldsFor(MeasureMode.GARMENT, Category.BOTTOM)
        Category.FULL_BODY -> FieldKey.entries.filter { it != FieldKey.NECK }
        Category.NECKWEAR -> listOf(FieldKey.NECK)
    }
    return if (basis == ChartBasis.BODY) keys else keys.filter { it != FieldKey.HEIGHT && it != FieldKey.NECK }
}
fun FieldKey.isFlatWidth(mode: MeasureMode): Boolean = mode == MeasureMode.GARMENT && this in listOf(FieldKey.CHEST, FieldKey.WAIST, FieldKey.HIPS)
fun canonicalValue(key: FieldKey, value: Long, mode: MeasureMode): Long = if (key.isFlatWidth(mode)) value * 2 else value
