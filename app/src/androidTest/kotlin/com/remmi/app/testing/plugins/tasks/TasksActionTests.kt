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

/**
 * FULL FLOW: TASKS
 */
class TasksFullFlowActionTest(
    private val actions: TasksActions
) : RemmiActionTest {
    override val name: String = "Tasks: Full Flow"
    override val pluginId: String = "tasks"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        return try {
            // 1. Add
            val addSuccess = actions.createTask(
                title = "Flow Task",
                description = "Testing full flow"
            )
            if (!addSuccess) throw IllegalStateException("Failed to add task")

            // 2. Get & Verify
            val tasks = actions.getAllTasks()
            val created = tasks.find { it.title == "Flow Task" } 
                ?: throw IllegalStateException("Task not found after creation")

            // 3. Update
            val updatedItem = created.copy(description = "Updated Flow Description")
            val updateSuccess = actions.updateTask(updatedItem)
            if (!updateSuccess) throw IllegalStateException("Failed to update task")

            // 4. Toggle
            actions.toggleTask(updatedItem)

            // 5. Delete
            val deleteSuccess = actions.deleteTask(created.id)
            if (!deleteSuccess) throw IllegalStateException("Failed to delete task")

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
