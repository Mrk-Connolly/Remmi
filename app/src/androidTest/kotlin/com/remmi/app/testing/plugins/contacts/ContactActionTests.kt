package com.remmi.app.testing.plugins.contacts

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.contacts.ContactActions
import java.util.UUID

/**
 * CONTACT ACTION TESTS
 */
class AddContactActionTest(
    private val actions: ContactActions
) : RemmiActionTest {
    override val name: String = "Contacts: Add Contact"
    override val pluginId: String = "contacts"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        val result = actions.createContact(
            name = "Diagnostic",
            surname = "Contact",
            nickname = "Test",
            phone = null,
            email = null,
            birthday = null,
            group = "FRIENDS"
        )
        
        val status = if (result) TestStatus.SUCCESS else TestStatus.FAILURE

        return DatabaseTestLog(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            pluginId = pluginId,
            operation = "ACTION: ADD_CONTACT",
            status = status
        )
    }
}
