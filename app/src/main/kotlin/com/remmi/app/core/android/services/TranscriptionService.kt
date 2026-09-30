package com.remmi.app.core.android.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.remmi.app.R
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * TRANSCRIPTION SERVICE
 *
 * Foreground service that manages high-priority microphone access for:
 * 1. Audio recording (MediaRecorder).
 * 2. Real-time speech transcription (SpeechRecognizer).
 */
class TranscriptionService : Service(), CommandListener, RecognitionListener {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var eventBus: EventBus? = null
    private lateinit var audioManager: AudioManager
    
    // Recording
    private var recorder: MediaRecorder? = null
    private var isRecording = false
    private var isPaused = false
    private var startTime: Long = 0
    private var currentFile: File? = null
    
    // Transcription
    private var speechRecognizer: SpeechRecognizer? = null
    private var currentTranscriptionId: String? = null
    private var fullTranscript = StringBuilder()
    private var language: String = "auto"
    private var isListening = false

    companion object {
        const val ACTION_START = "com.remmi.app.ACTION_START_TRANSCRIPTION"
        const val ACTION_STOP = "com.remmi.app.ACTION_STOP_TRANSCRIPTION"
        
        private const val CHANNEL_ID = "transcription_channel"
        private const val NOTIFICATION_ID = 6001

        /** Static EventBus reference for the service */
        var eventBus: EventBus? = null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("Remmi", "[TranscriptionService] - onCreate")
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        this.eventBus = TranscriptionService.eventBus
        
        eventBus?.let { bus ->
            scope.launch { bus.subscribeCommand(this@TranscriptionService) }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showForegroundNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Remmi", "[TranscriptionService] - onDestroy")
        stopEverything()
        eventBus?.unsubscribeCommand(this)
    }

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is StartRecordingCommand -> startTranscription(command)
            is PauseRecordingCommand -> pauseTranscription()
            is ResumeRecordingCommand -> resumeTranscription()
            is FinishRecordingCommand -> stopTranscription(command.transcriptionId)
        }
    }

    private suspend fun startTranscription(command: StartRecordingCommand) {
        if (isRecording) return
        
        Log.i("Remmi", "[TranscriptionService] - Starting transcription for: ${command.title}")
        
        // 0. Check availability
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.e("Remmi", "[TranscriptionService] - Speech recognition is NOT available on this device")
            eventBus?.postError(Exception("Speech recognition not available"))
            return
        }

        currentTranscriptionId = command.transcriptionId
        language = command.language
        fullTranscript.setLength(0)
        isPaused = false
        
        // 1. Setup Data Paths
        val file = File(filesDir, "recordings/${command.transcriptionId}.mp4")
        file.parentFile?.mkdirs()
        currentFile = file

        try {
            // CRITICAL SWAP: Start SpeechRecognizer FIRST to claim the mic priority
            Log.d("Remmi", "[TranscriptionService] - Initializing SpeechRecognizer first")
            isRecording = true // Set state early
            startTime = System.currentTimeMillis()
            
            startListening()

            // Wait a moment for speech engine to initialize before recorder starts
            delay(500)

            Log.d("Remmi", "[TranscriptionService] - Initializing MediaRecorder")
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder?.apply {
                // VOICE_COMMUNICATION is designed for VoIP and allows concurrent capture more reliably
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            eventBus?.publishEvent(RecordingStateChangedEvent(command.transcriptionId, "RECORDING"))
        } catch (e: Exception) {
            Log.e("Remmi", "[TranscriptionService] - Failed to start: ${e.message}")
            stopEverything()
        }
    }

    private fun startListening() {
        if (!isRecording || isPaused || isListening) return
        
        scope.launch(Dispatchers.Main) {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@TranscriptionService)
                    speechRecognizer?.setRecognitionListener(this@TranscriptionService)
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    if (language != "auto") {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                    } else {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    }
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra("android.speech.extra.DICTATION_MODE", true)
                    
                    // Lecture-optimized: tell the system to keep listening longer during pauses
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
                }
                
                // Mute system sound temporarily to avoid start/stop beeps
                setSystemMute(true)
                speechRecognizer?.startListening(intent)
                isListening = true
            } catch (e: Exception) {
                Log.e("Remmi", "[TranscriptionService] - Start listening failed: ${e.message}")
                isListening = false
                setSystemMute(false)
            }
        }
    }

    private fun setSystemMute(mute: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val flag = if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE
                audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, flag, 0)
                audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, flag, 0)
                audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, flag, 0)
            } else {
                @Suppress("DEPRECATION")
                audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, mute)
                @Suppress("DEPRECATION")
                audioManager.setStreamMute(AudioManager.STREAM_ALARM, mute)
                @Suppress("DEPRECATION")
                audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, mute)
            }
        } catch (e: Exception) {
            Log.w("Remmi", "[TranscriptionService] - Mute failed: ${e.message}")
        }
    }

    private suspend fun pauseTranscription() {
        isPaused = true
        isListening = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.pause()
        }
        speechRecognizer?.stopListening()
        setSystemMute(false)
        eventBus?.publishEvent(RecordingStateChangedEvent(currentTranscriptionId ?: "", "PAUSED"))
    }

    private suspend fun resumeTranscription() {
        isPaused = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.resume()
        }
        startListening()
        eventBus?.publishEvent(RecordingStateChangedEvent(currentTranscriptionId ?: "", "RECORDING"))
    }

    private suspend fun stopTranscription(id: String) {
        val duration = System.currentTimeMillis() - startTime
        stopEverything()
        
        val audioPath = currentFile?.absolutePath ?: ""
        val textPath = File(filesDir, "transcriptions/$id.txt").absolutePath
        val finalContent = fullTranscript.toString()
        
        // Ensure directory exists
        File(textPath).parentFile?.mkdirs()
        File(textPath).writeText(finalContent)
        
        eventBus?.publishEvent(
            TranscriptionFinishedEvent(
                transcriptionId = id,
                fullText = finalContent,
                audioFilePath = audioPath,
                textFilePath = textPath,
                durationMillis = duration
            )
        )
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopEverything() {
        Log.d("Remmi", "[TranscriptionService] - stopEverything")
        isRecording = false
        isListening = false
        isPaused = false
        setSystemMute(false)
        
        try {
            recorder?.apply {
                stop()
                release()
            }
            speechRecognizer?.apply {
                stopListening()
                destroy()
            }
        } catch (e: Exception) {
            Log.e("Remmi", "[TranscriptionService] - Error stopping: ${e.message}")
        }
        recorder = null
        speechRecognizer = null
    }

    // RecognitionListener
    override fun onReadyForSpeech(params: Bundle?) { 
        Log.d("Remmi", "[TranscriptionService] - onReadyForSpeech") 
    }
    
    override fun onBeginningOfSpeech() { 
        Log.d("Remmi", "[TranscriptionService] - onBeginningOfSpeech") 
    }
    
    override fun onRmsChanged(rmsdB: Float) {
        // High values (> 5) indicate voice/noise is being heard. 0 indicates silence/blocked mic.
        if (rmsdB > 2f) {
            Log.v("Remmi", "[TranscriptionService] - mic level: $rmsdB")
        }
    }
    
    override fun onBufferReceived(buffer: ByteArray?) {}
    
    override fun onEndOfSpeech() {
        Log.d("Remmi", "[TranscriptionService] - onEndOfSpeech")
        isListening = false
        if (isRecording && !isPaused) {
            scope.launch { 
                delay(300) // Aggressive restart for live lectures
                startListening() 
            }
        }
    }
    
    override fun onError(error: Int) {
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error (Mic busy?)"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech heard"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            else -> "Unknown error: $error"
        }
        Log.w("Remmi", "[TranscriptionService] - onError ($error): $message")
        
        isListening = false
        setSystemMute(false)
        
        if (isRecording && !isPaused) {
            val retryDelay = if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_AUDIO) {
                2000L // Reduced from 3000L for better lecture continuity
            } else {
                800L // Reduced from 1500L
            }
            
            scope.launch { 
                delay(retryDelay) 
                Log.d("Remmi", "[TranscriptionService] - Attempting restart after error")
                startListening() 
            }
        }
    }
    
    override fun onResults(results: Bundle?) {
        isListening = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val text = matches[0]
            if (text.isNotBlank()) {
                fullTranscript.append(text).append(". ")
                currentTranscriptionId?.let { id ->
                    scope.launch {
                        eventBus?.publishEvent(TranscriptionUpdatedEvent(id, fullTranscript.toString(), true))
                        
                        // Incremental File Backup
                        try {
                            val textPath = File(filesDir, "transcriptions/$id.txt")
                            textPath.parentFile?.mkdirs()
                            textPath.writeText(fullTranscript.toString())
                        } catch (e: Exception) {
                            Log.e("Remmi", "[TranscriptionService] - Failed to update backup file: ${e.message}")
                        }
                    }
                }
            }
        }
        
        if (isRecording && !isPaused) {
            scope.launch { 
                delay(500)
                startListening() 
            }
        }
    }
    
    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val partialText = matches[0]
            currentTranscriptionId?.let { id ->
                scope.launch {
                    eventBus?.publishEvent(TranscriptionUpdatedEvent(id, fullTranscript.toString() + partialText, false))
                }
            }
        }
    }
    
    override fun onEvent(eventType: Int, params: Bundle?) {}

    private fun showForegroundNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Transcription Active")
            .setContentText("Remmi is recording and transcribing audio")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Transcription Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
