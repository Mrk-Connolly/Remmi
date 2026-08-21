package com.remmi.app.testing

import com.remmi.app.testing.core.CoreManagersTest
import com.remmi.app.testing.plugins.PluginIntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class SystemTest {

    @Test
    fun runFullSystemDiagnostic() = runTest {
        // Run Core Tests
        val coreTest = CoreManagersTest()
        coreTest.setup()
        coreTest.testDatabaseManager()
        coreTest.testFileManager()
        coreTest.testAndroidManager()

        // Run Plugin Tests
        val pluginTest = PluginIntegrationTest()
        pluginTest.setup()
        pluginTest.testAllPluginsDatabase()
        pluginTest.testAllPluginsActions()
    }
}
