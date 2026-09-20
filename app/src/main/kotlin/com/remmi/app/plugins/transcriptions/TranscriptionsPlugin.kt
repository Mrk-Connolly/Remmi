package com.remmi.app.plugins.transcriptions

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.*
import com.remmi.app.core.plugin.BaseRemmiPlugin
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.ui.RemmiScreen
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.plugins.transcriptions.models.TranscriptionItem
import com.remmi.app.plugins.transcriptions.models.TranscriptionGroup
import com.remmi.app.plugins.transcriptions.ui.screens.TranscriptionsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TranscriptionsPlugin(
    metadata: PluginMetadata,
    eventBus: EventBus
) : BaseRemmiPlugin<TranscriptionItem>(metadata, eventBus, TranscriptionItem::class.java) {

    private val _repository = TranscriptionsRepository()
    private val _actions = TranscriptionsActions(_repository).apply {
        this.eventBus = this@TranscriptionsPlugin.eventBus
    }

    override val repository: TranscriptionsRepository get() = _repository
    override val actions: TranscriptionsActions get() = _actions

    override val widget: RemmiWidget by lazy { TranscriptionsWidgets.MainWidget(metadata, actions) }

    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable
        override fun Content(controller: RemmiController) {
            TranscriptionsScreen(actions, controller)
        }
    }

    override suspend fun initialize() {
        super.initialize()
        Log.d("Remmi", "[TranscriptionsPlugin] - Initializing")
    }

    override suspend fun onCommand(command: RemmiCommand) {
        super.onCommand(command)
        // Generic database load handled by BaseRemmiPlugin
    }

    override suspend fun onEvent(event: RemmiEvent) {
        super.onEvent(event)
        when (event) {
            is RecordingStateChangedEvent -> {
                val item = repository.get(event.transcriptionId)
                if (item != null) {
                    val updated = item.copy(status = event.state, modified = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()))
                    repository.update(updated)
                } else {
                    // Safety: Create a placeholder if not found, though actions should handle it
                    Log.w("Remmi", "[TranscriptionsPlugin] - Received state change for unknown transcription: ${event.transcriptionId}")
                }
            }
            is TranscriptionUpdatedEvent -> {
                val item = repository.get(event.transcriptionId)
                if (item != null) {
                    val updated = item.copy(transcript = event.partialText, modified = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()))
                    repository.update(updated)
                }
            }
            is TranscriptionFinishedEvent -> {
                val item = repository.get(event.transcriptionId)
                if (item != null) {
                    val updated = item.copy(
                        status = "FINISHED",
                        transcript = event.fullText,
                        audioFilePath = event.audioFilePath,
                        textFilePath = event.textFilePath,
                        durationMillis = event.durationMillis,
                        finishedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()),
                        modified = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
                    )
                    repository.update(updated)
                    // Persist final state
                    actions.updateTranscription(updated)
                }
            }
        }
    }

    override suspend fun handleDataFetched(event: DataFetchedEvent<*>) {
        if (event.items.isNotEmpty()) {
            val first = event.items[0]
            if (first is TranscriptionItem) {
                super.handleDataFetched(event)
            } else if (first is TranscriptionGroup) {
                @Suppress("UNCHECKED_CAST")
                _repository.updateGroups(event.items as List<TranscriptionGroup>)
            }
        }
    }

    override fun onLoad() {
        super.onLoad()
        // Fetch groups too
        CoroutineScope(Dispatchers.IO).launch {
            eventBus.publishCommand(
                FetchAllDataCommand(
                    tableName = "transcription_groups",
                    serializer = TranscriptionGroup.serializer(),
                    source = "transcriptions"
                )
            )
        }
    }
}
