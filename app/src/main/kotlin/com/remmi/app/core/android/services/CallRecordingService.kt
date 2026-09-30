package com.remmi.app.core.android.services

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.remmi.app.R
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * CALL RECORDING SERVICE
 *
 * Core Android service responsible for:
 * 1. Monitoring call state.
 * 2. Managing recording lifecycle.
 * 3. Displaying the recording prompt.
 */
class CallRecordingService : Service(), CommandListener {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var eventBus: EventBus? = null
    private var recorder: MediaRecorder? = null
    private var isRecording = false
    private var currentFile: File? = null
    private var startTime: Long = 0
    private var currentCallInfo: CallDetectedEvent? = null

    private var recordingMode: String = "ASK_EVERY_CALL" // Default

    companion object {
        const val ACTION_START_RECORDING = "com.remmi.app.ACTION_START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.remmi.app.ACTION_STOP_RECORDING"
        const val ACTION_IGNORE_CALL = "com.remmi.app.ACTION_IGNORE_CALL"
        
        private const val CHANNEL_ID = "call_recording_channel"
        private const val NOTIFICATION_ID = 5001
        private const val PROMPT_NOTIFICATION_ID = 5002

        /** Static EventBus reference for the service to use */
        var eventBus: EventBus? = null
    }

    private val callStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            
            Log.d("Remmi", "[CallRecordingService] - Phone state changed: $state, Number: $number")

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    handleIncomingCall(number)
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    handleCallStarted(number)
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    handleCallEnded()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("Remmi", "[CallRecordingService] - onCreate")
        registerReceiver(callStateReceiver, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
        createNotificationChannel()
        
        // Load initial recording mode
        val settings = getSharedPreferences("remmi_settings", Context.MODE_PRIVATE)
        recordingMode = settings.getString("call_recording_mode", "ASK_EVERY_CALL") ?: "ASK_EVERY_CALL"

        // Initialize instance eventBus from companion
        this.eventBus = CallRecordingService.eventBus

        // Subscribe to commands if EventBus is available
        eventBus?.let { bus ->
            scope.launch {
                bus.subscribeCommand(this@CallRecordingService)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == null) {
            showMonitoringNotification()
        }

        when (intent?.action) {
            ACTION_START_RECORDING -> {
                scope.launch { startRecording() }
                cancelPrompt()
            }
            ACTION_STOP_RECORDING -> {
                scope.launch { stopRecording() }
            }
            ACTION_IGNORE_CALL -> {
                cancelPrompt()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Remmi", "[CallRecordingService] - onDestroy")
        unregisterReceiver(callStateReceiver)
        
        val capturedCallInfo = currentCallInfo
        val finalEvent = stopRecordingSync(capturedCallInfo, keepForeground = false)
        
        eventBus?.let { bus ->
            finalEvent?.let { event ->
                scope.launch { bus.publishEvent(event) }
            }
            bus.unsubscribeCommand(this@CallRecordingService)
        }
    }

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is StartCallRecordingCommand -> startRecording()
            is StopCallRecordingCommand -> stopRecording()
            is SetCallRecordingModeCommand -> {
                this.recordingMode = command.mode
            }
        }
    }

    private fun handleIncomingCall(number: String?) {
        Log.i("Remmi", "[CallRecordingService] - Incoming call detected from: $number")
        currentCallInfo = CallDetectedEvent(
            phoneNumber = number,
            direction = "INCOMING"
        )
        currentCallInfo?.let { event ->
            scope.launch {
                eventBus?.publishEvent(event)
            }
        }
    }

    private fun handleCallStarted(number: String?) {
        Log.i("Remmi", "[CallRecordingService] - Call started. Mode: $recordingMode")
        if (currentCallInfo == null) {
            // Probably an outgoing call
            currentCallInfo = CallDetectedEvent(
                phoneNumber = number,
                direction = "OUTGOING"
            )
            currentCallInfo?.let { event ->
                scope.launch {
                    eventBus?.publishEvent(event)
                }
            }
        }

        when (recordingMode) {
            "ALWAYS_RECORD" -> scope.launch { startRecording() }
            "ASK_EVERY_CALL" -> showRecordingPrompt(currentCallInfo?.phoneNumber)
            "NEVER_RECORD" -> { /* Do nothing */ }
        }
    }

    private fun handleCallEnded() {
        Log.i("Remmi", "[CallRecordingService] - Call ended")
        cancelPrompt()
        val capturedCallInfo = currentCallInfo
        if (isRecording) {
            scope.launch { stopRecording(capturedCallInfo) }
        }
        currentCallInfo = null
    }

    private suspend fun startRecording() {
        if (isRecording) return
        
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.e("Remmi", "[CallRecordingService] - Missing RECORD_AUDIO permission")
            eventBus?.publishEvent(CallRecordingFailedEvent("Missing microphone permission"))
            return
        }
        
        try {
            val recordingId = UUID.randomUUID().toString()
            val file = File(filesDir, "recordings/$recordingId.m4a")
            file.parentFile?.mkdirs()
            currentFile = file

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder?.apply {
                // Try VOICE_COMMUNICATION first
                try {
                    setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                } catch (e: Exception) {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                }
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            startTime = System.currentTimeMillis()
            
            eventBus?.publishEvent(CallRecordingStartedEvent(recordingId, file.absolutePath))
            showOngoingNotification()
            Log.i("Remmi", "[CallRecordingService] - Recording started: $recordingId")
        } catch (e: Exception) {
            Log.e("Remmi", "[CallRecordingService] - Failed to start recording: ${e.message}")
            isRecording = false
            eventBus?.publishEvent(CallRecordingFailedEvent(e.message ?: "Unknown error"))
        }
    }

    private suspend fun stopRecording(callInfo: CallDetectedEvent? = currentCallInfo) {
        if (!isRecording) return
        val event = stopRecordingSync(callInfo, keepForeground = true)
        event?.let {
            eventBus?.publishEvent(it)
        }
    }

    private fun stopRecordingSync(callInfo: CallDetectedEvent?, keepForeground: Boolean = true): CallRecordingFinishedEvent? {
        if (!isRecording) return null
        var resultEvent: CallRecordingFinishedEvent? = null
        try {
            recorder?.apply {
                stop()
                release()
            }
            val duration = System.currentTimeMillis() - startTime
            val file = currentFile
            if (file != null && file.exists()) {
                resultEvent = CallRecordingFinishedEvent(
                    filePath = file.absolutePath, 
                    durationMillis = duration,
                    phoneNumber = callInfo?.phoneNumber,
                    direction = callInfo?.direction
                )
            }
        } catch (e: Exception) {
            Log.e("Remmi", "[CallRecordingService] - Error stopping recorder: ${e.message}")
        } finally {
            recorder = null
            isRecording = false
            currentFile = null
            if (keepForeground) {
                showMonitoringNotification()
            } else {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
        return resultEvent
    }

    private fun showRecordingPrompt(number: String?) {
        val startIntent = Intent(this, CallRecordingService::class.java).apply { action = ACTION_START_RECORDING }
        val ignoreIntent = Intent(this, CallRecordingService::class.java).apply { action = ACTION_IGNORE_CALL }
        
        val startPending = PendingIntent.getService(this, 0, startIntent, PendingIntent.FLAG_IMMUTABLE)
        val ignorePending = PendingIntent.getService(this, 1, ignoreIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Record this call?")
            .setContentText(number ?: "Unknown Number")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "Record", startPending) // Re-use launcher icon for now
            .addAction(R.drawable.ic_launcher_foreground, "Don't Record", ignorePending)
            .setFullScreenIntent(null, true) // Don't use full screen intent as per contract, just high priority
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(PROMPT_NOTIFICATION_ID, notification)
    }

    private fun cancelPrompt() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(PROMPT_NOTIFICATION_ID)
    }

    private fun showMonitoringNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Remmi Call Monitor")
            .setContentText("Monitoring for calls")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Use low priority for monitoring
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun showOngoingNotification() {
        val stopIntent = Intent(this, CallRecordingService::class.java).apply { action = ACTION_STOP_RECORDING }
        val stopPending = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🔴 Recording call")
            .setContentText("Remmi is capturing call audio")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPending)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Recording",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Used for call recording prompts and status"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
