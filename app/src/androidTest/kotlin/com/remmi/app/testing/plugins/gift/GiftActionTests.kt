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
