package com.remmi.app.testing.plugins

import com.remmi.app.testing.base.BaseIntegrationTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * PLUGIN INTEGRATION TEST
 * 
 * Verifies that all expected plugins are loaded and registered in the system.
 */
class PluginIntegrationTest : BaseIntegrationTest() {

    @Test
    fun pluginRegistry_queryLoadedPlugins_containsAllExpectedPlugins() {
        // Arrange
        val expectedPlugins = listOf(
            "alarm", "calendar", "contacts", "gift", 
            "ingredient_stock", "recipe_book", "tasks",
            "maps", "weather"
        )
        
        // Act
        val loadedPlugins = controller.pluginManager.plugins.keys
        
        // Assert
        assertThat(loadedPlugins).containsAtLeastElementsIn(expectedPlugins)
    }

    @Test
    fun loadedPlugins_queryMetadata_hasMatchingIdAndNonEmptyName() {
        // Arrange & Act & Assert
        controller.pluginManager.plugins.forEach { (id, plugin) ->
            assertThat(plugin.metadata.id).isEqualTo(id)
            assertThat(plugin.metadata.name).isNotEmpty()
        }
    }
}
