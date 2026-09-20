package com.remmi.app.plugins.transcriptions

import android.util.Log
import com.remmi.app.core.plugin.repository.MemoryRepository
import com.remmi.app.plugins.transcriptions.models.TranscriptionItem
import com.remmi.app.plugins.transcriptions.models.TranscriptionGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for Transcriptions.
 */
class TranscriptionsRepository : MemoryRepository<TranscriptionItem>() {

    private val _groups = MutableStateFlow<List<TranscriptionGroup>>(emptyList())
    val groups = _groups.asStateFlow()

    init {
        Log.d("Remmi", "[TranscriptionsRepository] - Constructor initialized")
    }

    fun updateGroups(newList: List<TranscriptionGroup>) {
        _groups.value = newList
    }

    fun getGroup(id: String?): TranscriptionGroup? {
        return _groups.value.find { it.id == id }
    }
}
