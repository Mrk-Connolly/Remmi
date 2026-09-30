package com.remmi.app.core.eventBus

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.remmi.app.core.eventBus.commands.CommandListener
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.EventListener
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
class EventBusTest {

    private lateinit var eventBus: EventBus

    @Before
    fun setup() {
        eventBus = EventBus()
    }

    @Test
    fun subscribedEvent_publishEvent_deliversEventToSubscriber() = runTest {
        // Arrange
        val mockListener = mockk<EventListener>(relaxed = true)
        eventBus.subscribeEvent(mockListener)
        eventBus.start()

        val event = object : RemmiEvent {
            override val eventId = "test-event"
            override val source = "test"
            override val type = EventType.CREATED
            override val creationContext = null
            override val deletionContext = null
            override val correlationId = null
            override val causationId = null
        }

        // Act
        eventBus.publishEvent(event)
        advanceUntilIdle()

        // Assert
        coVerify { mockListener.onEvent(event) }
    }

    @Test
    fun subscribedCommand_publishCommand_deliversCommandToSubscriber() = runTest {
        // Arrange
        val mockListener = mockk<CommandListener>(relaxed = true)
        eventBus.subscribeCommand(mockListener)
        eventBus.start()

        val command = object : RemmiCommand {
            override val commandId = "test-command"
            override val source = "test"
            override val creationContext = null
            override val deletionContext = null
            override val correlationId = null
            override val causationId = null
        }

        // Act
        eventBus.publishCommand(command)
        advanceUntilIdle()

        // Assert
        coVerify { mockListener.onCommand(command) }
    }

    @Test
    fun unsubscribedEvent_publishEvent_doesNotDeliverEventToSubscriber() = runTest {
        // Arrange
        val mockListener = mockk<EventListener>(relaxed = true)
        eventBus.subscribeEvent(mockListener)
        eventBus.start()
        eventBus.unsubscribeEvent(mockListener)

        val event = object : RemmiEvent {
            override val eventId = "test-event"
            override val source = "test"
            override val type = EventType.CREATED
            override val creationContext = null
            override val deletionContext = null
            override val correlationId = null
            override val causationId = null
        }

        // Act
        eventBus.publishEvent(event)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 0) { mockListener.onEvent(any()) }
    }

    @Test
    fun errorOccurred_postError_emitsErrorToStream() = runTest {
        // Arrange
        val exception = RuntimeException("Test Error")
        
        // Act & Assert
        eventBus.errors.test {
            eventBus.postError(exception)
            assertThat(awaitItem()).isEqualTo(exception)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun subscriptionsCleared_publishEventAndCommand_deliversNoMessages() = runTest {
        // Arrange
        val mockEventListener = mockk<EventListener>(relaxed = true)
        val mockCommandListener = mockk<CommandListener>(relaxed = true)
        
        eventBus.subscribeEvent(mockEventListener)
        eventBus.subscribeCommand(mockCommandListener)
        eventBus.start()
        
        eventBus.clear()

        val event = object : RemmiEvent {
            override val eventId = "e1"
            override val source = "test"
            override val type = EventType.CREATED
            override val creationContext = null
            override val deletionContext = null
            override val correlationId = null
            override val causationId = null
        }
        val command = object : RemmiCommand {
            override val commandId = "c1"
            override val source = "test"
            override val creationContext = null
            override val deletionContext = null
            override val correlationId = null
            override val causationId = null
        }

        // Act
        eventBus.publishEvent(event)
        eventBus.publishCommand(command)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 0) { mockEventListener.onEvent(any()) }
        coVerify(exactly = 0) { mockCommandListener.onCommand(any()) }
    }
}
