package com.remmi.app.plugins.callrecorder

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.remmi.app.plugins.callrecorder.models.CallRecording
import com.remmi.app.plugins.callrecorder.logic.CallRecordingManager
import com.remmi.app.plugins.callrecorder.logic.CallStateMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallRecorderService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var manager: CallRecordingManager
    private lateinit var monitor: CallStateMonitor

    companion object {
        private const val CHANNEL_ID = "call_recorder_channel"
        private const val NOTIFICATION_ID = 3001
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("Remmi", "[CallRecorderService] - Created")
        
        // In a real app, these would be injected or retrieved from a singleton
        // For this plugin, we'll initialize them here if not already done.
        manager = CallRecorderPlugin.getManager()
        
        monitor = CallStateMonitor(
            onCallStarted = { callInfo ->
                scope.launch {
                    manager.startRecording(callInfo)
                    updateNotification(true)
                }
            },
            onCallEnded = {
                scope.launch {
                    val recording: CallRecording? = manager.stopRecording()
                    if (recording != null) {
                        CallRecorderPlugin.getActions().saveRecording(recording)
                    }
                    updateNotification(false)
                }
            }
        )

        val filter = IntentFilter().apply {
            addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
            @Suppress("DEPRECATION")
            addAction(Intent.ACTION_NEW_OUTGOING_CALL)
        }
        registerReceiver(monitor, filter)
        
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                createNotification(false),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification(false))
        }
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(monitor)
        isRunning = false
        Log.d("Remmi", "[CallRecorderService] - Destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Call Recorder Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(active: Boolean): Notification {
        val title = if (active) "Recording Call..." else "Call Recorder Active"
        val text = if (active) "Capturing call audio" else "Waiting for calls"
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.presence_audio_busy)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(active: Boolean) {
        if (!CallRecorderPlugin.getActions().isNotificationEnabled()) return
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(active))
    }
}
