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

/**
 * FULL FLOW: CALENDAR
 */
class CalendarFullFlowActionTest(
    private val actions: CalendarActions
) : RemmiActionTest {
    override val name: String = "Calendar: Full Flow"
    override val pluginId: String = "calendar"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return try {
            // 1. Add
            val resultId = actions.addEvent(
                title = "Flow Test Event",
                description = "Testing full flow",
                startingDate = today
            ) ?: throw IllegalStateException("Failed to add event")

            // 2. Get & Verify
            val event = actions.getEvent(resultId) ?: throw IllegalStateException("Event not found after creation")

            // 3. Update
            val updatedItem = event.copy(title = "Updated Flow Event")
            val updateSuccess = actions.updateEvent(updatedItem)
            if (!updateSuccess) throw IllegalStateException("Failed to update event")

            // 4. Delete
            val deleteSuccess = actions.removeEvent(resultId)
            if (!deleteSuccess) throw IllegalStateException("Failed to delete event")

            DatabaseTestLog(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                pluginId = pluginId,
                operation = "ACTION: FULL_FLOW",
                status = TestStatus.SUCCESS
            )
        } catch (e: Exception) {
            DatabaseTestLog(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                pluginId = pluginId,
                operation = "ACTION: FULL_FLOW",
                status = TestStatus.FAILURE,
                errorMessage = e.message
            )
        }
    }
}
