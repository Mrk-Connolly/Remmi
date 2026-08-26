package com.remmi.app.testing.plugins.calendar

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.calendar.CalendarRepository
import kotlinx.datetime.*
import java.util.UUID

class CalendarDatabaseTest(
    private val repository: CalendarRepository,
    private val testRepository: DatabaseTestRepository
) : PluginDatabaseTest {
    override val pluginId: String = "calendar"

    override suspend fun runTests(): List<DatabaseTestLog> {
        val tester = GenericPluginTester(pluginId, repository, testRepository)
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        val item = CalendarItem(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            title = "Test Event",
            startingDate = today
        )
        
        val updated = item.copy(title = "Updated Test Event")
        
        return tester.runCrudFlow(item, updated)
    }
}
