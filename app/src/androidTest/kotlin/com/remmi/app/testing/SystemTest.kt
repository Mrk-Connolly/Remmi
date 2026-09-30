package com.remmi.app.testing

import com.remmi.app.testing.base.BaseIntegrationTest
import com.remmi.app.testing.core.CoreManagersTest
import com.remmi.app.testing.plugins.RelationshipIntegrationTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * SYSTEM TEST SUITE
 * 
 * Aggregates all integration tests for a full system diagnostic.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CoreManagersTest::class,
    RelationshipIntegrationTest::class
)
class SystemTestSuite

/**
 * LEGACY SYSTEM TEST (Refactored)
 * 
 * Main entry point for manual diagnostics.
 */
class SystemTest : BaseIntegrationTest() {

    @Test
    fun runFullSystemDiagnostic() {
        // This class now serves as a high-level placeholder.
        // Individual components should be tested in their own test classes.
    }
}
