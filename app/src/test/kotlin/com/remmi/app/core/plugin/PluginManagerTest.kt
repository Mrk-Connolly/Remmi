package com.remmi.app.core.plugin

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.EventType
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.RemmiEvent
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PluginManagerTest {

    private lateinit var context: Context
    private lateinit var eventBus: EventBus
    private lateinit var pluginManager: PluginManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        eventBus = EventBus()
        pluginManager = PluginManager(context, eventBus)
    }

    @Test
    fun subscribePlugins_publishedCommandAndEvent_deliversMessagesToSubscribedPlugins() = runTest {
        // Arrange
        val mockPlugin = mockk<RemmiPlugin>(relaxed = true)
        pluginManager.plugins["test-plugin"] = mockPlugin
        
        pluginManager.subscribePlugins(eventBus)
        eventBus.start()

        val command = object : RemmiCommand {
            override val commandId = "c1"
            override val source = "test"
            override val creationContext = null
            override val deletionContext = null
            override val correlationId = null
            override val causationId = null
        }
        val event = object : RemmiEvent {
            override val eventId = "e1"
            override val source = "test"
            override val type = EventType.CREATED
            override val creationContext = null
            override val deletionContext = null
            override val correlationId = null
            override val causationId = null
        }

        // Act
        eventBus.publishCommand(command)
        eventBus.publishEvent(event)
        advanceUntilIdle()

        // Assert
        coVerify { mockPlugin.onCommand(command) }
        coVerify { mockPlugin.onEvent(event) }
    }
}
