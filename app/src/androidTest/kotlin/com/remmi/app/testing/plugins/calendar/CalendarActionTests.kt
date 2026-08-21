package com.remmi.app.testing.plugins.calendar

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.calendar.CalendarActions
import kotlinx.datetime.*
import java.util.UUID

/**
 * CALENDAR ACTION TESTS
 */
class AddCalendarEventActionTest(
    private val actions: CalendarActions
) : RemmiActionTest {
    override val name: String = "Calendar: Add Event"
    override val pluginId: String = "calendar"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        val resultId = actions.addEvent(
            title = "Diagnostic Event",
            description = "Created by Remmi Diagnostic System",
            startingDate = today
        )
        
        val status = if (resultId != null) TestStatus.SUCCESS else TestStatus.FAILURE

        return DatabaseTestLog(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            pluginId = pluginId,
            operation = "ACTION: ADD_EVENT",
            status = status
        )
    }
}
