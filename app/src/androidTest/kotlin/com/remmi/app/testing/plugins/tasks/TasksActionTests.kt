package com.remmi.app.testing.plugins.tasks

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.tasks.TasksActions
import java.util.UUID

/**
 * TASK ACTION TESTS
 */
class AddTaskActionTest(
    private val actions: TasksActions
) : RemmiActionTest {
    override val name: String = "Tasks: Create Task"
    override val pluginId: String = "tasks"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        val result = actions.createTask(
            title = "Diagnostic Task",
            description = "Created by Remmi Diagnostic System"
        )
        
        val status = if (result) TestStatus.SUCCESS else TestStatus.FAILURE

        return DatabaseTestLog(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            pluginId = pluginId,
            operation = "ACTION: CREATE_TASK",
            status = status
        )
    }
}
