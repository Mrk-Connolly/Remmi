package com.remmi.app.core.android.speech

import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.StartRecordingCommand
import com.remmi.app.core.eventBus.commands.FinishRecordingCommand
import com.remmi.app.core.eventBus.events.RecordingStateChangedEvent
import com.remmi.app.core.eventBus.events.TranscriptionUpdatedEvent
import com.remmi.app.core.eventBus.events.TranscriptionFinishedEvent
import com.remmi.app.core.eventBus.events.EventListener
import com.remmi.app.core.eventBus.commands.CommandListener
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
class TranscriptionFlowTest {

    private lateinit var eventBus: EventBus

    @Before
    fun setup() {
        eventBus = EventBus()
    }

    @Test
    fun startRecording_publishedCommand_triggersStateChangeEvent() = runTest {
        // Arrange
        val mockListener = mockk<EventListener>(relaxed = true)
        eventBus.subscribeEvent(mockListener)
        eventBus.start()

        val transcriptionId = "test-id"
        val command = StartRecordingCommand(
            transcriptionId = transcriptionId,
            title = "Test Meeting",
            source = "test"
        )

        // Act
        eventBus.publishCommand(command)
        advanceUntilIdle()

        // Simulate service reaction publishing state change
        eventBus.publishEvent(RecordingStateChangedEvent(transcriptionId, "RECORDING"))
        advanceUntilIdle()

        // Assert
        coVerify { mockListener.onEvent(match { it is RecordingStateChangedEvent && it.transcriptionId == transcriptionId }) }
    }

    @Test
    fun transcriptionUpdated_publishedEvent_propagatesToSubscribers() = runTest {
        // Arrange
        val mockListener = mockk<EventListener>(relaxed = true)
        eventBus.subscribeEvent(mockListener)
        eventBus.start()

        val transcriptionId = "test-id"
        val partialText = "Hello world"
        
        val event = TranscriptionUpdatedEvent(
            transcriptionId = transcriptionId,
            partialText = partialText,
            isFinal = false,
            source = "test-service"
        )

        // Act
        eventBus.publishEvent(event)
        advanceUntilIdle()

        // Assert
        coVerify { mockListener.onEvent(event) }
    }

    @Test
    fun finishRecording_publishedCommandAndEvent_deliversFinishedEventToListener() = runTest {
        // Arrange
        val mockListener = mockk<EventListener>(relaxed = true)
        eventBus.subscribeEvent(mockListener)
        eventBus.start()

        val transcriptionId = "test-id"
        
        // Act
        eventBus.publishCommand(FinishRecordingCommand(transcriptionId, source = "test-ui"))
        advanceUntilIdle()

        val finishedEvent = TranscriptionFinishedEvent(
            transcriptionId = transcriptionId,
            fullText = "Complete transcript",
            audioFilePath = "/path/to/audio.mp4",
            textFilePath = "/path/to/text.txt",
            durationMillis = 5000,
            source = "test-service"
        )
        
        eventBus.publishEvent(finishedEvent)
        advanceUntilIdle()

        // Assert
        coVerify { mockListener.onEvent(finishedEvent) }
    }
}
