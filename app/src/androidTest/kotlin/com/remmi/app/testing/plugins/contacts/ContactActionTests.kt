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

/**
 * FULL FLOW: CONTACTS
 */
class ContactFullFlowActionTest(
    private val actions: ContactActions
) : RemmiActionTest {
    override val name: String = "Contacts: Full Flow"
    override val pluginId: String = "contacts"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        return try {
            // 1. Add
            val addSuccess = actions.createContact(
                name = "Flow",
                surname = "Test",
                nickname = "Flowy",
                phone = "123456",
                email = "flow@test.com",
                birthday = "1990-01-01",
                group = "WORK"
            )
            if (!addSuccess) throw IllegalStateException("Failed to add contact")

            // 2. Get & Verify
            val contacts = actions.getAllContacts()
            val created = contacts.find { it.name == "Flow" && it.surname == "Test" } 
                ?: throw IllegalStateException("Contact not found after creation")

            // 3. Update
            val updatedItem = created.copy(nickname = "Updated Flowy")
            val updateSuccess = actions.updateContact(updatedItem)
            if (!updateSuccess) throw IllegalStateException("Failed to update contact")

            // 4. Delete
            val deleteSuccess = actions.deleteContact(created.id)
            if (!deleteSuccess) throw IllegalStateException("Failed to delete contact")

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
