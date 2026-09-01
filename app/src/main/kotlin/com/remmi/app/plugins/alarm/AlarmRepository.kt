package com.remmi.app.plugins.alarm

import android.util.Log
import com.remmi.app.plugins.alarm.models.AlarmItem
import com.remmi.app.core.plugin.repository.MemoryRepository

/**
 * Repository for managing [AlarmItem] data.
 *
 * Provides local in-memory caching for alarms.
 */
class AlarmRepository : MemoryRepository<AlarmItem>() {

    init {
        Log.d("Remmi", "[AlarmRepository] - Constructor initialized")
    }
}
