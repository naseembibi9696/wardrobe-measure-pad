package com.wardrobemeasure.pad.data.local

import com.wardrobemeasure.pad.domain.model.*
import com.wardrobemeasure.pad.domain.usecase.ValidateChartUseCase
import org.json.JSONArray
import org.json.JSONObject

class NotebookSerializer {
    private fun json(vararg pairs: Pair<String, Any?>): JSONObject = JSONObject().apply {
        pairs.forEach { (key, value) -> put(key, value ?: JSONObject.NULL) }
    }
    private fun <T> array(items: List<T>, encode: (T) -> JSONObject): JSONArray = JSONArray().apply { items.forEach { put(encode(it)) } }
    private fun <T> objects(array: JSONArray, decode: (JSONObject) -> T): List<T> = (0 until array.length()).map { decode(array.getJSONObject(it)) }
    private fun values(values: Map<FieldKey, Long>): JSONObject = JSONObject().apply { values.forEach { (key, value) -> put(key.name, value) } }
    private fun readValues(json: JSONObject): Map<FieldKey, Long> = json.keys().asSequence().associate { FieldKey.valueOf(it) to json.getLong(it) }
    private fun nullable(json: JSONObject, key: String): String? = if (json.isNull(key)) null else json.getString(key)
    private fun ranges(values: Map<FieldKey, LengthRange>): JSONObject = JSONObject().apply {
        values.forEach { (key, range) -> put(key.name, json("min" to range.min, "max" to range.max)) }
    }
    private fun readRanges(obj: JSONObject): Map<FieldKey, LengthRange> = obj.keys().asSequence().associate { key ->
        val range = obj.getJSONObject(key)
        FieldKey.valueOf(key) to LengthRange(range.getLong("min"), range.getLong("max"))
    }
    private fun result(result: SavedResult): JSONObject = json(
        "id" to result.id, "kind" to result.kind.name, "profileId" to result.profileId, "garmentId" to result.garmentId,
        "sourceName" to result.sourceName, "values" to values(result.values), "chartId" to result.chartId,
        "chartTitle" to result.chartTitle, "size" to result.size, "matched" to result.matched, "timestamp" to result.timestamp,
        "chartBasis" to result.chartBasis?.name, "chartCategory" to result.chartCategory?.name, "chartBrand" to result.chartBrand,
        "outcomes" to array(result.outcomes) { json("key" to it.key.name, "raw" to it.raw, "value" to it.value,
            "min" to it.range.min, "max" to it.range.max, "outcome" to it.outcome.name, "gap" to it.gap) }
    )
    private fun readResult(obj: JSONObject): SavedResult = SavedResult(
        id = obj.getString("id"), kind = ResultKind.valueOf(obj.getString("kind")), profileId = obj.getString("profileId"),
        garmentId = nullable(obj, "garmentId"), sourceName = obj.getString("sourceName"), values = readValues(obj.getJSONObject("values")),
        chartId = nullable(obj, "chartId"), chartTitle = obj.getString("chartTitle"), size = obj.getString("size"),
        matched = obj.getBoolean("matched"), timestamp = obj.getLong("timestamp"),
        chartBasis = nullable(obj, "chartBasis")?.let { ChartBasis.valueOf(it) },
        chartCategory = nullable(obj, "chartCategory")?.let { Category.valueOf(it) }, chartBrand = obj.optString("chartBrand", ""),
        outcomes = objects(obj.getJSONArray("outcomes")) { FieldOutcome(FieldKey.valueOf(it.getString("key")), it.getLong("raw"),
            it.getLong("value"), LengthRange(it.getLong("min"), it.getLong("max")), Outcome.valueOf(it.getString("outcome")), it.getLong("gap")) }
    )
    fun encode(notebook: Notebook): String = json(
        "schemaVersion" to 1, "seedCompleted" to notebook.seedCompleted,
        "profiles" to array(notebook.profiles) { json("id" to it.id, "name" to it.name, "relationship" to it.relationship,
            "avatar" to it.avatar, "sample" to it.isSample, "updatedAt" to it.updatedAt) },
        "garments" to array(notebook.garments) { json("id" to it.id, "profileId" to it.profileId, "name" to it.name,
            "category" to it.category.name, "sample" to it.isSample) },
        "records" to array(notebook.records) { json("sourceId" to it.sourceId, "profileId" to it.profileId,
            "garmentId" to it.garmentId, "values" to values(it.values), "updatedAt" to it.updatedAt) },
        "drafts" to array(notebook.drafts) { json("sourceId" to it.sourceId, "values" to values(it.values), "revision" to it.revision, "confirmedAt" to it.confirmedAt) },
        "charts" to array(notebook.charts) { chart -> json("id" to chart.id, "brand" to chart.brand, "title" to chart.title,
            "basis" to chart.basis.name, "category" to chart.category.name, "sample" to chart.isSample, "updatedAt" to chart.updatedAt,
            "keys" to JSONArray(chart.keys.map { it.name }), "rows" to array(chart.rows) { json("id" to it.id, "label" to it.label, "ranges" to ranges(it.ranges)) }) },
        "settings" to json("unit" to notebook.settings.unit.name, "haptics" to notebook.settings.haptics, "selectedProfileId" to notebook.settings.selectedProfileId),
        "result" to notebook.result?.let { result(it) }
    ).toString()

    fun decode(text: String): Notebook {
        val root = JSONObject(text)
        require(root.getInt("schemaVersion") == 1)
        val settings = root.getJSONObject("settings")
        val notebook = Notebook(
            seedCompleted = root.getBoolean("seedCompleted"),
            profiles = objects(root.getJSONArray("profiles")) { Profile(it.getString("id"), it.getString("name"), it.getString("relationship"), it.getInt("avatar"), it.getBoolean("sample"), it.getLong("updatedAt")) },
            garments = objects(root.getJSONArray("garments")) { Garment(it.getString("id"), it.getString("profileId"), it.getString("name"), Category.valueOf(it.getString("category")), it.getBoolean("sample")) },
            records = objects(root.getJSONArray("records")) { MeasurementRecord(it.getString("sourceId"), it.getString("profileId"), nullable(it,"garmentId"), readValues(it.getJSONObject("values")), it.getLong("updatedAt")) },
            drafts = objects(root.getJSONArray("drafts")) { MeasurementDraft(it.getString("sourceId"), readValues(it.getJSONObject("values")), it.getLong("revision"), it.getLong("confirmedAt")) },
            charts = objects(root.getJSONArray("charts")) { obj ->
                val keys = obj.getJSONArray("keys")
                SizeChart(obj.getString("id"), obj.getString("brand"), obj.getString("title"), ChartBasis.valueOf(obj.getString("basis")),
                    Category.valueOf(obj.getString("category")), (0 until keys.length()).map { FieldKey.valueOf(keys.getString(it)) },
                    objects(obj.getJSONArray("rows")) { ChartRow(it.getString("id"), it.getString("label"), readRanges(it.getJSONObject("ranges"))) },
                    obj.getBoolean("sample"), obj.getLong("updatedAt"))
            },
            settings = NotebookSettings(UnitSystem.valueOf(settings.getString("unit")), settings.getBoolean("haptics"), nullable(settings, "selectedProfileId")),
            result = if (root.isNull("result")) null else readResult(root.getJSONObject("result"))
        )
        require(notebook.profiles.map { it.id }.distinct().size == notebook.profiles.size)
        require(notebook.profiles.all { it.name.trim().length in 1..30 && it.relationship.length <= 30 && it.avatar in 0..3 })
        val profileIds = notebook.profiles.map { it.id }.toSet()
        require(notebook.garments.map { it.id }.distinct().size == notebook.garments.size)
        require(notebook.garments.all { it.profileId in profileIds && it.name.trim().length in 1..40 && it.category != Category.NECKWEAR })
        val garmentIds = notebook.garments.map { it.id }.toSet()
        require(notebook.records.map { it.sourceId }.distinct().size == notebook.records.size)
        require(notebook.records.all { record -> record.profileId in profileIds &&
            record.sourceId == sourceId(record.profileId, record.garmentId) &&
            (record.garmentId == null || notebook.garments.any { it.id == record.garmentId && it.profileId == record.profileId }) &&
            record.values.all { MeasurementUnits.valid(it.value, it.key.isFlatWidth(if (record.garmentId == null) MeasureMode.BODY else MeasureMode.GARMENT)) } })
        require(notebook.drafts.map { it.sourceId }.distinct().size == notebook.drafts.size)
        require(notebook.drafts.all { draft -> (draft.sourceId in profileIds || draft.sourceId in garmentIds) &&
            draft.values.all { MeasurementUnits.valid(it.value, it.key.isFlatWidth(if (draft.sourceId in garmentIds) MeasureMode.GARMENT else MeasureMode.BODY)) } })
        require(notebook.charts.map { it.id }.distinct().size == notebook.charts.size)
        require(notebook.charts.all { ValidateChartUseCase()(it) })
        return notebook
    }
}
