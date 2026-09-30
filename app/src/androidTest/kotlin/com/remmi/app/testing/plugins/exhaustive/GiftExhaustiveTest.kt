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
class GiftExhaustiveTest : BasePluginActionTest() {

    @Test
    fun testGiftCrud_Success() = runTest {
        val plugin = controller.pluginManager.plugins["gift"] as GiftPlugin
        
        // 1. Create
        val success = plugin.actions.addGiftIdea(
            contactId = "c1",
            name = "Watch",
            description = "Expensive watch",
            link = "http://shop.com",
            price = 500.0,
            event = GiftEvent.Birthday
        )
        assertThat(success).isTrue()
        
        val ideas = plugin.actions.getGiftIdeasForContact("c1")
        val watch = ideas.find { it.name == "Watch" }
        assertThat(watch).isNotNull()
        
        // 2. Update
        val updated = watch!!.copy(name = "Premium Watch")
        val updateSuccess = plugin.actions.updateGiftIdea(updated)
        assertThat(updateSuccess).isTrue()
        
        // 3. Delete
        val deleteSuccess = plugin.actions.deleteGiftIdea(watch.id)
        assertThat(deleteSuccess).isTrue()
        
        val ideasAfter = plugin.actions.getGiftIdeasForContact("c1")
        assertThat(ideasAfter.any { it.id == watch.id }).isFalse()
    }
}
