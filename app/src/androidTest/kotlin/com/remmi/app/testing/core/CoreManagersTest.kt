package com.remmi.app.testing.core

import androidx.test.platform.app.InstrumentationRegistry
import com.remmi.app.core.controller.RemmiController
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class CoreManagersTest {

    private lateinit var controller: RemmiController

    @Before
    fun setup() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        controller = RemmiController(appContext)
        controller.start()
    }

    @Test
    fun testDatabaseManager() = runTest {
        assertNotNull(controller.databaseManager)
        assertNotNull(controller.databaseManager.service)
    }

    @Test
    fun testFileManager() = runTest {
        assertNotNull(controller.fileManager)
        assertNotNull(controller.fileManager.service)
    }

    @Test
    fun testAndroidManager() = runTest {
        assertNotNull(controller.androidManager)
        assertNotNull(controller.androidManager.alarmService)
        assertNotNull(controller.androidManager.notificationService)
        assertNotNull(controller.androidManager.weatherService)
    }
}
