package com.remmi.app.testing.base

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.remmi.app.core.android.files.FileService
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.database.DatabaseService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
abstract class BaseIntegrationTest {

    protected lateinit var controller: RemmiController
    protected val context get() = ApplicationProvider.getApplicationContext<Context>()
    
    protected lateinit var mockDb: DatabaseService
    protected lateinit var mockFile: FileService
    
    protected val testDispatcher = UnconfinedTestDispatcher()

    @Before
    open fun setup() = runTest {
        mockDb = mockk(relaxed = true)
        mockFile = mockk(relaxed = true)
        
        val pluginsJson = """
            [
                {"id": "alarm", "name": "Alarms", "version": "1.0", "author": "Remmi", "enabled": true, "showInNavigation": true, "showWidget": true, "group": "Tools"},
                {"id": "calendar", "name": "Calendar", "version": "1.0", "author": "Remmi", "enabled": true, "showInNavigation": true, "showWidget": true, "group": "Tools"},
                {"id": "contacts", "name": "Contacts", "version": "1.0", "author": "Remmi", "enabled": true, "showInNavigation": true, "showWidget": true, "group": "Tools"},
                {"id": "tasks", "name": "Tasks", "version": "1.0", "author": "Remmi", "enabled": true, "showInNavigation": true, "showWidget": true, "group": "Tools"},
                {"id": "ingredient_stock", "name": "Ingredients", "version": "1.0", "author": "Remmi", "enabled": true, "showInNavigation": true, "showWidget": true, "group": "Tools"},
                {"id": "recipe_book", "name": "Recipes", "version": "1.0", "author": "Remmi", "enabled": true, "showInNavigation": true, "showWidget": true, "group": "Tools"},
                {"id": "gift", "name": "Gifts", "version": "1.0", "author": "Remmi", "enabled": true, "showInNavigation": true, "showWidget": true, "group": "Tools"},
                {"id": "maps", "name": "Maps", "version": "1.0", "author": "Remmi", "enabled": true, "showInNavigation": true, "showWidget": true, "group": "Tools"},
                {"id": "call_recorder", "name": "Call Recorder", "version": "1.0", "author": "Remmi", "enabled": true, "showInNavigation": true, "showWidget": true, "group": "Tools"},
                {"id": "transcriptions", "name": "Transcriptions", "version": "1.0", "author": "Remmi", "enabled": true, "showInNavigation": true, "showWidget": true, "group": "Tools"},
                {"id": "weather", "name": "Weather", "version": "1.0", "author": "Remmi", "enabled": true, "showInNavigation": true, "showWidget": true, "group": "Tools"}
            ]
        """.trimIndent()
        
        every { mockFile.readText(any(), any()) } returns pluginsJson
        every { mockFile.exists(any()) } returns false
        
        controller = RemmiController(context, mockDb, mockFile, testDispatcher)
        controller.start()
        
        advanceUntilIdle()
    }

    @After
    open fun tearDown() {
        clearAllMocks()
    }
}
