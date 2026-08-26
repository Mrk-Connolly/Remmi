package com.remmi.app.testing.plugins.alarm

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.alarm.models.AlarmItem
import com.remmi.app.plugins.alarm.AlarmRepository
import java.util.UUID

class AlarmDatabaseTest(
    private val repository: AlarmRepository,
    private val testRepository: DatabaseTestRepository
) : PluginDatabaseTest {
    override val pluginId: String = "alarm"

    override suspend fun runTests(): List<DatabaseTestLog> {
        val tester = GenericPluginTester(pluginId, repository, testRepository)
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        val item = AlarmItem(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            title = "Test Alarm",
            time = now
        )
        
        val updated = item.copy(title = "Updated Test Alarm")
        
        return tester.runCrudFlow(item, updated)
    }
}
