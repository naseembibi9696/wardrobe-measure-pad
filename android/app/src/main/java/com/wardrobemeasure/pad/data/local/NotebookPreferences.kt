package com.wardrobemeasure.pad.data.local

import android.content.Context

class NotebookPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("wardrobe_measure_pad", Context.MODE_PRIVATE)
    fun current(): String? = preferences.getString("snapshot", null)
    fun previous(): String? = preferences.getString("previous_snapshot", null)
    fun commit(snapshot: String, previousValid: String?): Boolean {
        val editor = preferences.edit().putString("snapshot", snapshot)
        if (previousValid != null) editor.putString("previous_snapshot", previousValid)
        return editor.commit()
    }
}
