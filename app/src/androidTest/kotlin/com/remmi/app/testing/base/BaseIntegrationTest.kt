package com.remmi.app.testing.base

import androidx.test.platform.app.InstrumentationRegistry
import com.remmi.app.core.controller.RemmiController
import io.mockk.clearAllMocks
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

/**
 * BASE INTEGRATION TEST
 * 
 * Provides common infrastructure for instrumented integration tests.
 */
abstract class BaseIntegrationTest {

    protected lateinit var controller: RemmiController
    protected val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    open fun setup() = runTest {
        // Use the existing MockDatabaseService for consistency with current patterns
        controller = RemmiController(context, MockDatabaseService())
        controller.start()
    }

    @After
    open fun tearDown() {
        clearAllMocks()
    }
}
