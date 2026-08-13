package com.remmi.app.plugins.calendar

import android.util.Log
import com.remmi.app.core.repository.CloudRepository
import com.remmi.app.core.service.DatabaseService
import kotlinx.datetime.*

/**
 * Repository implementation for managing [CalendarItem] data.
 */
class CalendarRepository (databaseService: DatabaseService) : CloudRepository<CalendarItem>(
    databaseService = databaseService,
    tableName = "calendar",
    serializer = CalendarItem.serializer()
) {

    init {
        Log.d("Remmi", "[CalendarRepository] - [constructor] executed")
        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        // Add a default sample event to ensure the UI has something to display initially.
        add(
            CalendarItem(
                id = "calendar_001",
                created = now,
                modified = now,
                title = "New Calendar Event",
                description = "Default description",
                startingDate = today,
                startingTime = LocalTime(9, 0),
                endingDate = today,
                endingTime = LocalTime(10, 0),
                isPriority = true,
                group = "General",
                participants = emptyList(),
                repeat = emptyList(),
                location = emptyList(),
                linkedTasks = emptyList(),
                linkedAlarm = null,
            )
        )
    }
}
