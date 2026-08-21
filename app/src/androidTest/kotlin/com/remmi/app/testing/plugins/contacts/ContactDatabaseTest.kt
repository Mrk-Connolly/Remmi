package com.remmi.app.testing.plugins.contacts

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.contacts.ContactItem
import com.remmi.app.plugins.contacts.ContactRepository
import java.util.UUID

class ContactDatabaseTest(
    private val repository: ContactRepository,
    private val testRepository: DatabaseTestRepository
) : PluginDatabaseTest {
    override val pluginId: String = "contacts"

    override suspend fun runTests(): List<DatabaseTestLog> {
        val tester = GenericPluginTester(pluginId, repository, testRepository)
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        val item = ContactItem(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            name = "Test",
            surname = "User",
            group = "FRIENDS"
        )
        
        val updated = item.copy(name = "Updated Test")
        
        return tester.runCrudFlow(item, updated)
    }
}
