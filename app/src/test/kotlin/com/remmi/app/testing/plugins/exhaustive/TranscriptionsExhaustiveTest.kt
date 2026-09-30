package com.remmi.app.testing.plugins.exhaustive

import com.google.common.truth.Truth.assertThat
import com.remmi.app.plugins.transcriptions.TranscriptionsPlugin
import com.remmi.app.plugins.transcriptions.models.TranscriptionItem
import com.remmi.app.testing.base.BasePluginActionTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import java.util.UUID

/**
 * TRANSCRIPTIONS EXHAUSTIVE TEST
 */
open class TranscriptionsExhaustiveTest : BasePluginActionTest() {

    @Test
    fun updateTranscription_validItem_persistsUpdatesAndDeletesTranscription() = runTest {
        // Arrange
        val plugin = controller.pluginManager.plugins["transcriptions"] as TranscriptionsPlugin
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        
        val transcription = TranscriptionItem(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            title = "Test Lecture",
            language = "en",
            status = "COMPLETED"
        )
        
        // Act 1: Add/Update
        plugin.actions.updateTranscription(transcription)

        // Assert 1
        var list = plugin.actions.getTranscriptions().first()
        assertThat(list.any { it.id == transcription.id }).isTrue()
        
        // Act 2: Update Title
        val updated = transcription.copy(title = "Updated Lecture")
        plugin.actions.updateTranscription(updated)

        // Assert 2
        list = plugin.actions.getTranscriptions().first()
        assertThat(list.find { it.id == transcription.id }!!.title).isEqualTo("Updated Lecture")
        
        // Act 3: Delete
        plugin.actions.deleteTranscription(transcription.id)

        // Assert 3
        list = plugin.actions.getTranscriptions().first()
        assertThat(list.any { it.id == transcription.id }).isFalse()
    }
}
