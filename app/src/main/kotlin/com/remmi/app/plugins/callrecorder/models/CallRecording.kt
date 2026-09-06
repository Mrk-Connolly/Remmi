package com.remmi.app.plugins.callrecorder.models

import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class CallDirection {
    INCOMING, OUTGOING
}

@Serializable
enum class RecordingStatus {
    RECORDING, COMPLETED, FAILED, UNSUPPORTED
}

@Serializable
data class CallRecording(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    override val userId: String? = null,
    override val sourcePlugin: String? = "call_recorder",
    override val sourceItemId: String? = null,

    val filePath: String,
    val durationMillis: Long = 0,
    val direction: CallDirection,
    val phoneNumber: String? = null,
    val contactName: String? = null,
    val status: RecordingStatus = RecordingStatus.COMPLETED,
    val errorMessage: String? = null,
    val group: String? = null,
    val appPackage: String? = null
) : RemmiModel
