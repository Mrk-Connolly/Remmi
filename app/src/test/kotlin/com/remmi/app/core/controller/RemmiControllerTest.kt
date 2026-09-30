package com.remmi.app.core.controller

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.remmi.app.core.database.DatabaseManager
import com.remmi.app.core.database.DatabaseService
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * REMMI CONTROLLER UNIT TEST
 * 
 * Demonstrates the use of Robolectric and MockK for unit testing core logic.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RemmiControllerTest {

    private lateinit var context: Context
    private lateinit var mockDatabaseService: DatabaseService

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockDatabaseService = mockk(relaxed = true)
    }

    @Test
    fun controllerInitialized_withMockDatabase_setsUpContextAndDatabaseManager() {
        // Arrange & Act
        val controller = RemmiController(context, mockDatabaseService)
        
        // Assert
        assertThat(controller.androidContext).isEqualTo(context)
        assertThat(controller.databaseManager).isNotNull()
        assertThat(controller.databaseManager.service).isEqualTo(mockDatabaseService)
    }

    @Test
    fun controllerCreated_queryContainerForDatabaseManager_returnsDatabaseManager() {
        // Arrange
        val controller = RemmiController(context, mockDatabaseService)
        
        // Act
        val dbManager = controller.container.get(DatabaseManager::class)
        
        // Assert
        assertThat(dbManager).isNotNull()
        assertThat(dbManager.service).isEqualTo(mockDatabaseService)
    }
}
