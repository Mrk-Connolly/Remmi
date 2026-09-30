package com.remmi.app.core.android.media

import com.remmi.app.core.eventBus.commands.CommandListener

/**
 * AUDIO PLAYER SERVICE
 *
 * Interface for controlling audio playback.
 */
interface AudioPlayerService : CommandListener {
    fun play(filePath: String)
    fun stop()
    fun release()
}
