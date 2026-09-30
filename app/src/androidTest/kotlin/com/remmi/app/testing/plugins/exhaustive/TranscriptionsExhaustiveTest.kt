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
class TranscriptionsExhaustiveTest : BasePluginActionTest() {

    @Test
    fun testTranscriptionCrud_Success() = runTest {
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
        
        // 1. Add (via internal repository or update)
        // Note: Actions don't have a direct 'add' but 'startRecording' creates one.
        // We can test updateTranscription with a manual item.
        plugin.actions.updateTranscription(transcription)
        
        var list = plugin.actions.getTranscriptions().first()
        assertThat(list.any { it.id == transcription.id }).isTrue()
        
        // 2. Update
        val updated = transcription.copy(title = "Updated Lecture")
        plugin.actions.updateTranscription(updated)
        list = plugin.actions.getTranscriptions().first()
        assertThat(list.find { it.id == transcription.id }!!.title).isEqualTo("Updated Lecture")
        
        // 3. Delete
        plugin.actions.deleteTranscription(transcription.id)
        list = plugin.actions.getTranscriptions().first()
        assertThat(list.any { it.id == transcription.id }).isFalse()
    }
}
