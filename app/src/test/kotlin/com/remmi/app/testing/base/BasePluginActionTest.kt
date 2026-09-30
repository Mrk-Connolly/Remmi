package com.remmi.app.testing.base

import com.remmi.app.core.database.DatabaseService
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.plugin.model.models.RemmiModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.KSerializer
import org.junit.Before

/**
 * BASE PLUGIN ACTION TEST
 * 
 * Provides failure simulation for plugin action integration tests.
 */
abstract class BasePluginActionTest : BaseIntegrationTest() {

    protected lateinit var failingDatabase: DatabaseService

    @Before
    override fun setup() {
        super.setup()
        failingDatabase = mockk(relaxed = true)
    }

    protected fun simulateDatabaseFailure() {
        coEvery { 
            controller.databaseManager.service.insert(any(), any<RemmiModel>(), any<KSerializer<RemmiModel>>()) 
        } throws RuntimeException("Simulated Database Failure")
        
        coEvery { 
            controller.databaseManager.service.update(any(), any<RemmiModel>(), any<KSerializer<RemmiModel>>()) 
        } throws RuntimeException("Simulated Database Failure")
        
        coEvery { 
            controller.databaseManager.service.delete(any(), any()) 
        } throws RuntimeException("Simulated Database Failure")
    }

    protected fun simulateNetworkError() {
        // Mocking Supabase specific errors if needed
        coEvery { 
            controller.databaseManager.service.getAll(any(), any<KSerializer<RemmiModel>>()) 
        } throws java.io.IOException("Network Unavailable")
    }
}
