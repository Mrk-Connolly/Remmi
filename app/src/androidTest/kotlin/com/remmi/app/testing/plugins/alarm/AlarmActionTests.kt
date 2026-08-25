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
            status = status,
            sourcePlugin = null,
            sourceItemId = null
        )
    }
}
