package com.remmi.app.core.android.speech.implementations

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.remmi.app.core.android.speech.SpeechService
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.TranscriptionUpdatedEvent
import com.remmi.app.core.eventBus.events.TranscriptionFinishedEvent
import com.remmi.app.core.android.files.FileService
import kotlinx.coroutines.*
import java.io.File
import java.util.Locale

/**
 * SYSTEM SPEECH SERVICE
 *
 * Android SpeechRecognizer based implementation of SpeechService.
 */
class SystemSpeechService(
    private val context: Context,
    private val eventBus: EventBus,
    private val fileService: FileService,
    private val recordingService: AndroidRecordingService // We link them here for coordination
) : SpeechService, RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var currentTranscriptionId: String? = null
    private var fullTranscript = StringBuilder()
    private var isRecording = false
    private var language: String = "auto"

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is StartRecordingCommand -> {
                currentTranscriptionId = command.transcriptionId
                language = command.language
                fullTranscript.setLength(0)
                isRecording = true
                startListening(language)
            }
            is PauseRecordingCommand -> {
                if (currentTranscriptionId == command.transcriptionId) {
                    stopListening()
                }
            }
            is ResumeRecordingCommand -> {
                if (currentTranscriptionId == command.transcriptionId) {
                    startListening(language)
                }
            }
            is FinishRecordingCommand -> {
                if (currentTranscriptionId == command.transcriptionId) {
                    isRecording = false
                    stopListening()
                    finalizeTranscription(command.transcriptionId)
                }
            }
        }
    }

    override suspend fun startListening(language: String) {
        withContext(Dispatchers.Main) {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(this@SystemSpeechService)
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                if (language != "auto") {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                } else {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                }
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                // Some devices support continuous
                putExtra("android.speech.extra.DICTATION_MODE", true)
            }

            speechRecognizer?.startListening(intent)
        }
    }

    override suspend fun stopListening() {
        withContext(Dispatchers.Main) {
            speechRecognizer?.stopListening()
        }
    }

    private suspend fun finalizeTranscription(id: String) {
        val duration = recordingService.stopRecording()
        val audioPath = File(context.filesDir, "recordings/$id.mp4").absolutePath
        val textPath = File(context.filesDir, "transcriptions/$id.txt").absolutePath
        
        val finalContent = fullTranscript.toString()
        
        // Save TXT file
        fileService.writeText("transcriptions/$id.txt", finalContent)
        
        eventBus.publishEvent(
            TranscriptionFinishedEvent(
                transcriptionId = id,
                fullText = finalContent,
                audioFilePath = audioPath,
                textFilePath = textPath,
                durationMillis = duration
            )
        )
    }

    // RecognitionListener implementation
    override fun onReadyForSpeech(params: Bundle?) { Log.d("Remmi", "[SystemSpeechService] - Ready for speech") }
    override fun onBeginningOfSpeech() { Log.d("Remmi", "[SystemSpeechService] - Beginning of speech") }
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        Log.d("Remmi", "[SystemSpeechService] - End of speech")
        if (isRecording) {
            // Restart if it stopped but we are still in recording mode
            CoroutineScope(Dispatchers.Main).launch { startListening(language) }
        }
    }

    override fun onError(error: Int) {
        Log.e("Remmi", "[SystemSpeechService] - Error: $error")
        if (isRecording && error != SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
            CoroutineScope(Dispatchers.Main).launch { 
                delay(1000)
                startListening(language) 
            }
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val text = matches[0]
            fullTranscript.append(" ").append(text)
            currentTranscriptionId?.let { id ->
                CoroutineScope(Dispatchers.Default).launch {
                    eventBus.publishEvent(TranscriptionUpdatedEvent(id, fullTranscript.toString(), true))
                }
            }
        }
        if (isRecording) {
            CoroutineScope(Dispatchers.Main).launch { startListening(language) }
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val partialText = matches[0]
            currentTranscriptionId?.let { id ->
                CoroutineScope(Dispatchers.Default).launch {
                    eventBus.publishEvent(TranscriptionUpdatedEvent(id, fullTranscript.toString() + " " + partialText, false))
                }
            }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
