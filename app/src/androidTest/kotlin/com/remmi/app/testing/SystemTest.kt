package com.remmi.app.testing

import android.util.Log
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
        val results = mutableListOf<String>()
        Log.i("RemmiDiag", "Starting Full System Diagnostic...")

        // 1. Core Managers
        val coreTest = CoreManagersTest()
        runStep("Core: Setup", results) { coreTest.setup() }
        runStep("Core: Database Manager", results) { coreTest.testDatabaseManager() }
        runStep("Core: File Service", results) { coreTest.testFileService() }
        runStep("Core: Android Manager", results) { coreTest.testAndroidManager() }

        // 2. Plugin Database Integration
        val pluginTest = PluginIntegrationTest()
        runStep("Plugins: Setup", results) { pluginTest.setup() }
        
        val plugins = listOf("alarm", "calendar", "contacts", "gift", "ingredient_stock", "recipe_book", "tasks")
        plugins.forEach { pluginId ->
            runStep("DB: $pluginId", results) {
                pluginTest.testSpecificPluginDatabase(pluginId)
            }
        }
        
        // 3. Plugin Actions Integration
        runStep("Plugins: Action Integration", results) { pluginTest.testAllPluginsActionsDiagnostic() }

        // Summary
        Log.i("RemmiDiag", "====================================================")
        Log.i("RemmiDiag", "             SYSTEM DIAGNOSTIC SUMMARY              ")
        Log.i("RemmiDiag", "====================================================")
        results.forEach { Log.i("RemmiDiag", it) }
        Log.i("RemmiDiag", "====================================================")
    }

    private suspend fun runStep(name: String, results: MutableList<String>, block: suspend () -> Unit) {
        try {
            block()
            results.add("✅ PASS: $name")
        } catch (t: Throwable) {
            results.add("❌ FAIL: $name -> ${t.message}")
            Log.e("RemmiDiag", "Error in step $name", t)
        }
    }
}
