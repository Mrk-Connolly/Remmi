package com.remmi.app.plugins.calendar

import com.remmi.app.core.model.components.TimeRange
import com.remmi.app.core.repository.MemoryRepository
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import com.remmi.app.core.model.components.Metadata
import com.remmi.app.core.model.components.Priority
import com.remmi.app.core.model.components.RepeatRule
import com.remmi.app.core.model.components.RepeatType

class CalendarRepository : MemoryRepository<CalendarItem>(

) {

    init {
        add(

            CalendarItem(
                id = "calendar_001",
                created = Clock.System.now(),
                modified = Clock.System.now(),
                title = "New Calendar Event",
                description = "Default description",
                time = TimeRange(
                    start = LocalDateTime(
                        year = 2026,
                        monthNumber = 8,
                        dayOfMonth = 12,
                        hour = 9,
                        minute = 30
                    ).toInstant(TimeZone.currentSystemDefault())
                ),
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