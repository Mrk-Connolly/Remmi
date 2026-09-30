package com.remmi.app.testing.plugins.exhaustive

import com.google.common.truth.Truth.assertThat
import com.remmi.app.plugins.contacts.ContactPlugin
import com.remmi.app.testing.base.BasePluginActionTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * CONTACT EXHAUSTIVE TEST
 */
class ContactExhaustiveTest : BasePluginActionTest() {

    @Test
    fun testContactCrud_Success() = runTest {
        val plugin = controller.pluginManager.plugins["contacts"] as ContactPlugin
        
        // 1. Create
        val success = plugin.actions.createContact(
            name = "John",
            surname = "Doe",
            nickname = "Johnny",
            phone = "123456789",
            email = "john@example.com",
            birthday = "1990-01-01",
            group = "Friends"
        )
        assertThat(success).isTrue()
        
        var contacts = plugin.actions.getAllContacts()
        val john = contacts.find { it.name == "John" && it.surname == "Doe" }
        assertThat(john).isNotNull()
        assertThat(john!!.nickname).isEqualTo("Johnny")
        
        // 2. Update (Toggle Favorite)
        val toggleSuccess = plugin.actions.toggleFavorite(john)
        assertThat(toggleSuccess).isTrue()
        
        val updatedJohn = plugin.actions.getAllContacts().find { it.id == john.id }
        assertThat(updatedJohn!!.isFavorite).isTrue()
        
        // 3. Delete
        val deleteSuccess = plugin.actions.deleteContact(john.id)
        assertThat(deleteSuccess).isTrue()
        
        contacts = plugin.actions.getAllContacts()
        assertThat(contacts.any { it.id == john.id }).isFalse()
    }
}
