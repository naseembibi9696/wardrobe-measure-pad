package com.wardrobemeasure.pad.data.repository

import com.wardrobemeasure.pad.data.local.NotebookPreferences
import com.wardrobemeasure.pad.data.local.NotebookSerializer
import com.wardrobemeasure.pad.data.sample.SampleData
import com.wardrobemeasure.pad.domain.model.*
import com.wardrobemeasure.pad.domain.repository.NotebookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class NotebookRepositoryImpl(private val preferences: NotebookPreferences, private val serializer: NotebookSerializer) : NotebookRepository {
    private val mutableState = MutableStateFlow(NotebookState())
    override val state = mutableState.asStateFlow()
    private val mutex = Mutex()
    private var validSnapshot: String? = null

    override suspend fun load(): Unit = mutex.withLock {
        withContext(Dispatchers.IO) {
            mutableState.value = mutableState.value.copy(loading = true, error = false)
            runCatching {
                val current = preferences.current()
                val previous = preferences.previous()
                val parsed = current?.let { runCatching { serializer.decode(it) }.getOrNull() }
                val recovered = if (parsed == null) previous?.let { runCatching { serializer.decode(it) }.getOrNull() } else null
                when {
                    parsed != null -> { validSnapshot = current; mutableState.value = NotebookState(false, parsed) }
                    recovered != null -> { validSnapshot = previous; mutableState.value = NotebookState(false, recovered, recovered = true) }
                    current == null && previous == null -> {
                        val seed = SampleData.notebook()
                        val encoded = serializer.encode(seed)
                        if (preferences.commit(encoded, null)) {
                            validSnapshot = encoded
                            mutableState.value = NotebookState(false, seed)
                        } else mutableState.value = NotebookState(loading = false, error = true)
                    }
                    else -> mutableState.value = NotebookState(loading = false, error = true)
                }
            }.onFailure { mutableState.value = mutableState.value.copy(loading = false, error = true) }
            Unit
        }
    }

    override suspend fun change(transform: (Notebook) -> Notebook): Boolean = mutex.withLock {
        if (mutableState.value.loading || mutableState.value.error) return@withLock false
        withContext(Dispatchers.IO) {
            runCatching {
                val next = transform(mutableState.value.notebook)
                val encoded = serializer.encode(next)
                if (!preferences.commit(encoded, validSnapshot)) return@runCatching false
                validSnapshot = encoded
                mutableState.value = mutableState.value.copy(notebook = next)
                true
            }.getOrDefault(false)
        }
    }
}
