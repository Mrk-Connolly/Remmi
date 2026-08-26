package com.remmi.app.testing.plugins.maps

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.maps.MapsActions
import java.util.UUID

/**
 * MAPS ACTION TESTS
 */
class AddSavedLocationActionTest(
    private val actions: MapsActions
) : RemmiActionTest {
    override val name: String = "Maps: Add Location"
    override val pluginId: String = "maps"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        actions.saveLocation(
            name = "Diagnostic Point",
            address = "123 Test St",
            lat = 45.0,
            lon = 9.0
        )

        return DatabaseTestLog(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            pluginId = pluginId,
            operation = "ACTION: ADD_LOCATION",
            status = TestStatus.SUCCESS
        )
    }
}

/**
 * FULL FLOW: MAPS
 */
class MapsFullFlowActionTest(
    private val actions: MapsActions
) : RemmiActionTest {
    override val name: String = "Maps: Full Flow"
    override val pluginId: String = "maps"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        return try {
            // 1. Add
            val created = actions.saveLocation(
                name = "Flow Point",
                address = "456 Flow Ave",
                lat = 46.0,
                lon = 10.0
            )

            // 2. Get & Verify
            val locations = actions.getAllSavedLocations()
            if (locations.none { it.id == created.id }) {
                throw IllegalStateException("Location not found after creation")
            }

            // 3. Delete
            actions.deleteLocation(created.id)

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
