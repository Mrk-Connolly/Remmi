package com.remmi.app.testing.plugins.exhaustive

import com.google.common.truth.Truth.assertThat
import com.remmi.app.plugins.maps.MapsPlugin
import com.remmi.app.testing.base.BasePluginActionTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * MAPS EXHAUSTIVE TEST
 */
open class MapsExhaustiveTest : BasePluginActionTest() {

    @Test
    fun saveLocation_validCoordinates_persistsAndDeletesLocation() = runTest {
        // Arrange
        val plugin = controller.pluginManager.plugins["maps"] as MapsPlugin
        
        // Act 1: Create
        val location = plugin.actions.saveLocation(
            name = "Home",
            address = "My Secret House",
            lat = 45.0,
            lon = 9.0
        )

        // Assert 1
        assertThat(location.name).isEqualTo("Home")
        var locations = plugin.actions.getAllSavedLocations()
        assertThat(locations.any { it.id == location.id }).isTrue()
        
        // Act 2: Delete
        plugin.actions.deleteLocation(location.id)

        // Assert 2
        locations = plugin.actions.getAllSavedLocations()
        assertThat(locations.any { it.id == location.id }).isFalse()
    }
}
