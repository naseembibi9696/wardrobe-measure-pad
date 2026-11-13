package com.wardrobemeasure.pad.domain.repository

import com.wardrobemeasure.pad.domain.model.Notebook
import com.wardrobemeasure.pad.domain.model.NotebookState
import kotlinx.coroutines.flow.StateFlow

interface NotebookRepository {
    val state: StateFlow<NotebookState>
    suspend fun load()
    suspend fun change(transform: (Notebook) -> Notebook): Boolean
}
