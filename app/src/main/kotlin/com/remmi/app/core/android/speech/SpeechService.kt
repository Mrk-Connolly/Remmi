package com.remmi.app.core.android.speech

import com.remmi.app.core.eventBus.commands.CommandListener

/**
 * SPEECH SERVICE
 *
 * Interface for speech transcription operations.
 */
interface SpeechService : CommandListener {
    suspend fun startListening(language: String = "auto")
    suspend fun stopListening()
}
