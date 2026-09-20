package com.remmi.app.plugins.transcriptions.models

import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class TranscriptSegment(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    override val userId: String? = null,
    override val sourcePlugin: String? = null,
    override val sourceItemId: String? = null,

    @SerialName("transcription_id")
    val transcriptionId: String,
    @SerialName("start_time_millis")
    val startTimeMillis: Long,
    @SerialName("end_time_millis")
    val endTimeMillis: Long,
    val text: String,
    @SerialName("speaker_id")
    val speakerId: String? = null
) : RemmiModel
