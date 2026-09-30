package com.remmi.app.core.controller

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.remmi.app.core.android.files.FileService
import com.remmi.app.core.database.DatabaseManager
import com.remmi.app.core.database.DatabaseService
import com.remmi.app.core.plugin.PluginManager
import com.remmi.app.core.android.services.AndroidServiceManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RemmiControllerLifecycleTest {

    private lateinit var context: Context
    private lateinit var mockDatabaseService: DatabaseService
    private lateinit var mockFileService: FileService

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockDatabaseService = mockk(relaxed = true)
        mockFileService = mockk(relaxed = true)
        
        coEvery { mockFileService.readText("plugins.json") } returns "[]"
    }

    @Test
    fun `test controller startup registers all managers`() {
        val controller = RemmiController(context, mockDatabaseService, mockFileService)
        
        assertThat(controller.container.get(DatabaseManager::class)).isNotNull()
        assertThat(controller.container.get(PluginManager::class)).isNotNull()
        assertThat(controller.container.get(AndroidServiceManager::class)).isNotNull()
    }

    @Test
    fun `test controller lifecycle state`() = runTest {
        val controller = RemmiController(context, mockDatabaseService, mockFileService)
        assertThat(controller.isStarted).isFalse()

        controller.start()
        assertThat(controller.isStarted).isTrue()

        controller.stop()
    }
}
