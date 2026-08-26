package com.remmi.app.testing.plugins.gift

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.gift.GiftActions
import java.util.UUID

/**
 * GIFT ACTION TESTS
 */
class AddGiftIdeaActionTest(
    private val actions: GiftActions
) : RemmiActionTest {
    override val name: String = "Gift: Add Idea"
    override val pluginId: String = "gift"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        val result = actions.addGiftIdea(
            contactId = "diagnostic_contact",
            name = "Diagnostic Gift",
            description = "Created by Remmi Diagnostic System",
            link = null,
            price = 10.0,
            event = null
        )
        
        val status = if (result) TestStatus.SUCCESS else TestStatus.FAILURE

        return DatabaseTestLog(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            pluginId = pluginId,
            operation = "ACTION: ADD_GIFT_IDEA",
            status = status
        )
    }
}

/**
 * FULL FLOW: GIFT
 */
class GiftFullFlowActionTest(
    private val actions: GiftActions
) : RemmiActionTest {
    override val name: String = "Gift: Full Flow"
    override val pluginId: String = "gift"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        return try {
            // 1. Add
            val addSuccess = actions.addGiftIdea(
                contactId = "flow_test_contact",
                name = "Flow Gift",
                description = "Testing full flow",
                link = "http://test.com",
                price = 25.0,
                event = null
            )
            if (!addSuccess) throw IllegalStateException("Failed to add gift idea")

            // 2. Get & Verify
            val ideas = actions.getGiftIdeasForContact("flow_test_contact")
            val created = ideas.find { it.name == "Flow Gift" } 
                ?: throw IllegalStateException("Gift idea not found after creation")

            // 3. Update
            val updatedItem = created.copy(description = "Updated Flow Description")
            val updateSuccess = actions.updateGiftIdea(updatedItem)
            if (!updateSuccess) throw IllegalStateException("Failed to update gift idea")

            // 4. Delete
            val deleteSuccess = actions.deleteGiftIdea(created.id)
            if (!deleteSuccess) throw IllegalStateException("Failed to delete gift idea")

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
