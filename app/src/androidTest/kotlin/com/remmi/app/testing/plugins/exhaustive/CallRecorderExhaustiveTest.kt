package com.remmi.app.testing.plugins.exhaustive

import com.google.common.truth.Truth.assertThat
import com.remmi.app.plugins.callrecorder.CallRecorderPlugin
import com.remmi.app.plugins.callrecorder.models.*
import com.remmi.app.testing.base.BasePluginActionTest
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import java.util.UUID

/**
 * CALL RECORDER EXHAUSTIVE TEST
 */
class CallRecorderExhaustiveTest : BasePluginActionTest() {

    @Test
    fun testRecordingMetadata_Success() = runTest {
        val plugin = controller.pluginManager.plugins["callrecorder"] as CallRecorderPlugin
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        
        val recording = CallRecording(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            filePath = "/sdcard/rec1.m4a",
            durationMillis = 5000,
            direction = CallDirection.INCOMING,
            phoneNumber = "12345",
            status = RecordingStatus.COMPLETED
        )
        
        // 1. Save
        plugin.actions.saveRecording(recording)
        var recordings = plugin.actions.recordings.value
        assertThat(recordings.any { it.id == recording.id }).isTrue()
        
        // 2. Add to Group
        plugin.actions.addToGroup(recording, "Work")
        recordings = plugin.actions.recordings.value
        assertThat(recordings.find { it.id == recording.id }!!.group).isEqualTo("Work")
        
        // 3. Delete
        plugin.actions.deleteRecording(recording)
        recordings = plugin.actions.recordings.value
        assertThat(recordings.any { it.id == recording.id }).isFalse()
    }
}
