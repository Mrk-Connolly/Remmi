package com.remmi.app.core.android.media.implementations

import android.media.MediaPlayer
import android.util.Log
import com.remmi.app.core.android.media.AudioPlayerService
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.commands.PlayAudioCommand
import com.remmi.app.core.eventBus.commands.StopAudioCommand

/**
 * ANDROID AUDIO PLAYER SERVICE
 *
 * MediaPlayer-based implementation of AudioPlayerService.
 */
class AndroidAudioPlayerService : AudioPlayerService {

    private var mediaPlayer: MediaPlayer? = null

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is PlayAudioCommand -> play(command.filePath)
            is StopAudioCommand -> stop()
        }
    }

    override fun play(filePath: String) {
        try {
            stop()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
            }
            Log.d("Remmi", "[AndroidAudioPlayerService] - Playing: $filePath")
        } catch (e: Exception) {
            Log.e("Remmi", "[AndroidAudioPlayerService] - Failed to play audio: ${e.message}")
        }
    }

    override fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("Remmi", "[AndroidAudioPlayerService] - Error stopping playback: ${e.message}")
        }
    }

    override fun release() {
        stop()
    }
}
