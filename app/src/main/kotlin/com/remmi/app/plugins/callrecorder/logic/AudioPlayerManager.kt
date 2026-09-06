package com.remmi.app.plugins.callrecorder.logic

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    
    private val _currentPlayingPath = MutableStateFlow<String?>(null)
    val currentPlayingPath = _currentPlayingPath.asStateFlow()

    fun play(filePath: String) {
        if (_currentPlayingPath.value == filePath && _isPlaying.value) {
            pause()
            return
        }

        try {
            stop()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPlayingPath.value = null
                }
            }
            _isPlaying.value = true
            _currentPlayingPath.value = filePath
            Log.d("Remmi", "[AudioPlayerManager] - Playing: $filePath")
        } catch (e: Exception) {
            Log.e("Remmi", "[AudioPlayerManager] - Failed to play: ${e.message}")
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                Log.d("Remmi", "[AudioPlayerManager] - Paused")
            } else {
                it.start()
                _isPlaying.value = true
                Log.d("Remmi", "[AudioPlayerManager] - Resumed")
            }
        }
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        _currentPlayingPath.value = null
    }
}
