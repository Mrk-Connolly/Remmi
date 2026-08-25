package com.remmi.app.testing.plugins.tasks

import com.remmi.app.testing.core.*
import com.remmi.app.core.model.tasks.TaskItem
import com.remmi.app.plugins.tasks.TasksRepository
import java.util.UUID

class TaskDatabaseTest(
    private val repository: TasksRepository,
    private val testRepository: DatabaseTestRepository
) : PluginDatabaseTest {
    override val pluginId: String = "tasks"

    override suspend fun runTests(): List<DatabaseTestLog> {
        val tester = GenericPluginTester(pluginId, repository, testRepository)
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        val item = TaskItem(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            title = "Test Task"
        )
        
        val updated = item.copy(title = "Updated Test Task")
        
        return tester.runCrudFlow(item, updated)
    }
}
