package com.remmi.app.testing.plugins.gift

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.gift.GiftIdea
import com.remmi.app.plugins.gift.GiftRepository
import java.util.UUID

class GiftDatabaseTest(
    private val repository: GiftRepository,
    private val testRepository: DatabaseTestRepository
) : PluginDatabaseTest {
    override val pluginId: String = "gift"

    override suspend fun runTests(): List<DatabaseTestLog> {
        val tester = GenericPluginTester(pluginId, repository, testRepository)
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        val item = GiftIdea(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            contactId = "fake_contact",
            name = "Test Gift"
        )
        
        val updated = item.copy(name = "Updated Test Gift")
        
        return tester.runCrudFlow(item, updated)
    }
}
