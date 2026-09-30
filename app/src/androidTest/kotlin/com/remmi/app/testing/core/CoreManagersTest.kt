package com.remmi.app.testing.core

import com.remmi.app.testing.base.BaseIntegrationTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * CORE MANAGERS TEST
 * 
 * Verifies that the core system components are correctly initialized and registered.
 */
class CoreManagersTest : BaseIntegrationTest() {

    @Test
    fun databaseManager_queryService_returnsNonNullService() {
        // Arrange & Act & Assert
        assertThat(controller.databaseManager).isNotNull()
        assertThat(controller.databaseManager.service).isNotNull()
    }

    @Test
    fun fileService_queryService_returnsNonNullService() {
        // Arrange & Act & Assert
        assertThat(controller.androidManager.fileService).isNotNull()
    }

    @Test
    fun androidManager_queryServices_returnsNonNullServices() {
        // Arrange & Act & Assert
        assertThat(controller.androidManager).isNotNull()
        assertThat(controller.androidManager.alarmService).isNotNull()
        assertThat(controller.androidManager.notificationService).isNotNull()
        assertThat(controller.androidManager.weatherService).isNotNull()
    }
}
