package com.remmi.app.plugins.transcriptions.models

import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class TranscriptionItem(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    override val userId: String? = null,
    override val sourcePlugin: String? = "transcriptions",
    override val sourceItemId: String? = null,

    val title: String,
    val description: String? = null,
    @SerialName("group_id")
    val groupId: String? = null,
    val language: String = "auto",
    val status: String = "CREATED",
    @SerialName("started_at")
    val startedAt: Instant? = null,
    @SerialName("finished_at")
    val finishedAt: Instant? = null,
    @SerialName("duration_millis")
    val durationMillis: Long = 0,
    @SerialName("audio_file_path")
    val audioFilePath: String? = null,
    @SerialName("text_file_path")
    val textFilePath: String? = null,
    val transcript: String? = null
) : RemmiModel
