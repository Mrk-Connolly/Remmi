package com.remmi.app.testing.plugins.alarm

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.alarm.AlarmActions
import java.util.UUID

/**
 * ALARM ACTION TESTS
 * 
 * Catalog of high-level action tests for the Alarm plugin.
 */
class AddAlarmActionTest(
    private val actions: AlarmActions
) : RemmiActionTest {
    override val name: String = "Alarm: Add Alarm"
    override val pluginId: String = "alarm"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val status = if (actions.addAlarm(
            title = "Diagnostic Test Alarm",
            description = "Created by Remmi Diagnostic System",
            time = now
        )) TestStatus.SUCCESS else TestStatus.FAILURE

        return DatabaseTestLog(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            pluginId = pluginId,
            operation = "ACTION: ADD_ALARM",
            status = status
        )
    }
}

/**
 * FULL FLOW: ALARM
 */
class AlarmFullFlowActionTest(
    private val actions: AlarmActions
) : RemmiActionTest {
    override val name: String = "Alarm: Full Flow"
    override val pluginId: String = "alarm"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        return try {
            // 1. Add
            val success = actions.addAlarm(
                title = "Flow Test Alarm",
                description = "Testing full flow",
                time = now.plus(kotlin.time.Duration.parse("1h"))
            )
            if (!success) throw IllegalStateException("Failed to add alarm")

            // 2. Get All & Verify
            val alarms = actions.getAllAlarms()
            val created = alarms.find { it.alarm.title == "Flow Test Alarm" } ?: throw IllegalStateException("Alarm not found after creation")

            // 3. Update
            val updatedItem = created.alarm.copy(title = "Updated Flow Alarm")
            val updateSuccess = actions.updateAlarm(updatedItem)
            if (!updateSuccess) throw IllegalStateException("Failed to update alarm")

            // 4. Delete
            val deleteSuccess = actions.deleteAlarm(created.alarm.id)
            if (!deleteSuccess) throw IllegalStateException("Failed to delete alarm")

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
