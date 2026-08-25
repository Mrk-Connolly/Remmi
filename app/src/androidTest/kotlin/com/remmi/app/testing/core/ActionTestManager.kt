package com.remmi.app.testing.core

import android.util.Log

/**
 * ACTION TEST MANAGER
 * 
 * Central registry for all self-executable plugin action tests.
 */
class ActionTestManager(private val testRepository: DatabaseTestRepository) {

    private val tests = mutableMapOf<String, RemmiActionTest>()

    /**
     * Register a new test in the catalog.
     */
    fun registerTest(test: RemmiActionTest) {
        tests[test.name] = test
        Log.d("RemmiTest", "Registered action test: ${test.name} for plugin ${test.pluginId}")
    }

    /**
     * Get all available test names.
     */
    fun getAvailableTests(): List<String> = tests.keys.sorted()

    /**
     * Execute a specific test by name and log result.
     */
    suspend fun executeTest(name: String): DatabaseTestLog? {
        val test = tests[name] ?: run {
            Log.e("RemmiTest", "Test not found in catalog: $name")
            return null
        }

        Log.i("RemmiTest", "Executing Action Test: $name...")
        val result = test.execute()
        testRepository.add(result)
        Log.i("RemmiTest", "Action Test $name finished with status: ${result.status}")
        
        return result
    }

    /**
     * Run all registered tests. Throws exception if any test fails.
     */
    suspend fun runAllWithExceptions() {
        val failures = mutableListOf<String>()
        tests.keys.forEach { name ->
            val result = executeTest(name)
            if (result?.status == TestStatus.FAILURE) {
                failures.add("$name: ${result.errorMessage}")
            }
        }
        
        if (failures.isNotEmpty()) {
            throw IllegalStateException("Action Tests Failed:\n${failures.joinToString("\n")}")
        }
    }

    /**
     * Run all tests and return a summary of failures without throwing.
     */
    suspend fun runDiagnostic(): List<String> {
        val failures = mutableListOf<String>()
        tests.keys.forEach { name ->
            try {
                val result = executeTest(name)
                if (result?.status == TestStatus.FAILURE) {
                    failures.add("$name: ${result.errorMessage}")
                }
            } catch (e: Exception) {
                failures.add("$name: Crashed with ${e.message}")
            }
        }
        return failures
    }
}
