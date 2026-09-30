package com.remmi.app.plugins.transcriptions

import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.plugin.actions.BaseRemmiAction
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.*
import com.remmi.app.plugins.transcriptions.models.TranscriptionItem
import com.remmi.app.plugins.transcriptions.models.TranscriptionGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class TranscriptionsActions(
    val repository: TranscriptionsRepository
) : BaseRemmiAction {
    override val id: String = "transcriptions"
    override val name: String = "Transcriptions"
    override var eventBus: EventBus? = null

    fun getTranscriptions(): Flow<List<TranscriptionItem>> = repository.asFlow()
    fun getGroups(): Flow<List<TranscriptionGroup>> = repository.groups

    override suspend fun sync() {
        eventBus?.publishCommand(
            FetchAllDataCommand(
                tableName = "transcriptions",
                serializer = TranscriptionItem.serializer(),
                source = "transcriptions"
            )
        )
        eventBus?.publishCommand(
            FetchAllDataCommand(
                tableName = "transcription_groups",
                serializer = TranscriptionGroup.serializer(),
                source = "transcriptions"
            )
        )
    }

    suspend fun startRecording(id: String, title: String, description: String?, groupId: String?, language: String) {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        val item = TranscriptionItem(
            id = id,
            created = now,
            modified = now,
            title = title,
            description = description,
            groupId = groupId,
            language = language,
            status = "RECORDING",
            startedAt = now
        )

        // 0. Add to local repository immediately so UI can react
        repository.add(item)

        // 1. Persist initial record
        eventBus?.publishCommand(
            UpsertDataCommand(
                tableName = "transcriptions",
                item = item,
                serializer = TranscriptionItem.serializer(),
                source = "transcriptions"
            )
        )

        // 2. Start Android recording/transcription
        eventBus?.publishCommand(
            StartRecordingCommand(
                transcriptionId = id,
                title = title,
                language = language,
                source = "transcriptions"
            )
        )
    }

    suspend fun pauseRecording(id: String) {
        eventBus?.publishCommand(PauseRecordingCommand(id, source = "transcriptions"))
    }

    suspend fun resumeRecording(id: String) {
        eventBus?.publishCommand(ResumeRecordingCommand(id, source = "transcriptions"))
    }

    suspend fun finishRecording(id: String) {
        eventBus?.publishCommand(FinishRecordingCommand(id, source = "transcriptions"))
    }

    suspend fun updateTranscription(item: TranscriptionItem) {
        repository.update(item)
        eventBus?.publishCommand(
            UpsertDataCommand(
                tableName = "transcriptions",
                item = item,
                serializer = TranscriptionItem.serializer(),
                source = "transcriptions"
            )
        )
    }

    suspend fun deleteTranscription(id: String) {
        repository.remove(id)
        eventBus?.publishCommand(
            DeleteDataCommand(
                tableName = "transcriptions",
                itemId = id,
                source = "transcriptions"
            )
        )
    }

    suspend fun createGroup(name: String) {
        val id = "group_" + UUID.randomUUID().toString().substring(0, 8)
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val group = TranscriptionGroup(
            id = id,
            created = now,
            modified = now,
            name = name
        )
        eventBus?.publishCommand(
            UpsertDataCommand(
                tableName = "transcription_groups",
                item = group,
                serializer = TranscriptionGroup.serializer(),
                source = "transcriptions"
            )
        )
    }

    suspend fun playAudio(filePath: String) {
        eventBus?.publishCommand(PlayAudioCommand(filePath, source = "transcriptions"))
    }

    suspend fun openFile(filePath: String) {
        eventBus?.publishCommand(OpenFileCommand(filePath, source = "transcriptions"))
    }

    suspend fun testTranscription(id: String) {
        // Simulate real-time transcription updates for UI testing
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
            eventBus?.publishEvent(RecordingStateChangedEvent(id, "RECORDING"))
            val sentences = listOf(
                "Hello, I am testing the transcription UI.",
                "It seems to be working correctly.",
                "Let's see if the text appears in real-time.",
                "This is a mock transcription sentence.",
                "End of test."
            )
            val fullText = StringBuilder()
            for (sentence in sentences) {
                kotlinx.coroutines.delay(2000)
                fullText.append(sentence).append(" ")
                eventBus?.publishEvent(TranscriptionUpdatedEvent(id, fullText.toString(), true))
            }
        }
    }
}
