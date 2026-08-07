package com.remmi.app.plugins.calendar

import com.remmi.app.core.model.components.Priority
import com.remmi.app.core.model.components.RepeatRule
import com.remmi.app.core.model.components.RepeatType
import com.remmi.app.core.model.components.TimeRange
import com.remmi.app.core.repository.CloudRepository
import com.remmi.app.core.service.DatabaseService

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * Repository implementation for managing [CalendarItem] data.
 *
 * This repository inherits from [CloudRepository], providing built-in synchronization
 * with the Supabase backend while maintaining an in-memory cache for fast access.
 */
class CalendarRepository (databaseService: DatabaseService) : CloudRepository<CalendarItem>(
    databaseService = databaseService,
    tableName = "calendar",
    serializer = CalendarItem.serializer()
) {

    init {
        // Add a default sample event to ensure the UI has something to display initially.
        add(
            CalendarItem(
                id = "calendar_001",
                created = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                modified = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                title = "New Calendar Event",
                description = "Default description",
                startingTime = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                endingTime = null,
                priority = Priority.HIGH,
                participants = mutableListOf(),
                repeat = RepeatRule(RepeatType.YEARLY),
                reminders = mutableListOf(),
                location = null,
                linkedTasks = mutableListOf(),
                linkedAlarm = null,
                relationships = mutableListOf()
            )
        )
    }
}
