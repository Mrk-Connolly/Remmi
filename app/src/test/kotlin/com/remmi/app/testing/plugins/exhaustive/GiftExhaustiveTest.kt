package com.remmi.app.testing.plugins.exhaustive

import com.google.common.truth.Truth.assertThat
import com.remmi.app.plugins.gift.GiftPlugin
import com.remmi.app.plugins.gift.models.GiftEvent
import com.remmi.app.testing.base.BasePluginActionTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * GIFT EXHAUSTIVE TEST
 */
open class GiftExhaustiveTest : BasePluginActionTest() {

    @Test
    fun addGiftIdea_validDetails_createsUpdatesAndDeletesGiftIdea() = runTest {
        // Arrange
        val plugin = controller.pluginManager.plugins["gift"] as GiftPlugin
        
        // Act 1: Create
        val success = plugin.actions.addGiftIdea(
            contactId = "c1",
            name = "Watch",
            description = "Expensive watch",
            link = "http://shop.com",
            price = 500.0,
            event = GiftEvent.Birthday
        )

        // Assert 1
        assertThat(success).isTrue()
        val ideas = plugin.actions.getGiftIdeasForContact("c1")
        val watch = ideas.find { it.name == "Watch" }
        assertThat(watch).isNotNull()
        
        // Act 2: Update
        val updated = watch!!.copy(name = "Premium Watch")
        val updateSuccess = plugin.actions.updateGiftIdea(updated)

        // Assert 2
        assertThat(updateSuccess).isTrue()
        
        // Act 3: Delete
        val deleteSuccess = plugin.actions.deleteGiftIdea(watch.id)

        // Assert 3
        assertThat(deleteSuccess).isTrue()
        val ideasAfter = plugin.actions.getGiftIdeasForContact("c1")
        assertThat(ideasAfter.any { it.id == watch.id }).isFalse()
    }
}
