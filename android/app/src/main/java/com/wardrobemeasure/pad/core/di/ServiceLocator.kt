package com.wardrobemeasure.pad.core.di

import android.content.Context
import com.wardrobemeasure.pad.data.local.NotebookPreferences
import com.wardrobemeasure.pad.data.local.NotebookSerializer
import com.wardrobemeasure.pad.data.repository.NotebookRepositoryImpl

object ServiceLocator {
    private var initializedFactory: AppViewModelFactory? = null
    val factory: AppViewModelFactory get() = checkNotNull(initializedFactory)
    fun initialize(context: Context) {
        if (initializedFactory == null) initializedFactory = AppViewModelFactory(
            NotebookRepositoryImpl(NotebookPreferences(context.applicationContext), NotebookSerializer()))
    }
}
