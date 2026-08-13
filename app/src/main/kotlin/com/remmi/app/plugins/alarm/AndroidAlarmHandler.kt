package com.remmi.app.plugins.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.remmi.app.core.service.AndroidService
import kotlinx.datetime.Instant
import java.util.Calendar

class AndroidAlarmHandler(private val manualContext: Context? = null) : AndroidService {

    init {
        Log.d("Remmi", "[AndroidAlarmHandler] - [constructor] executed")
    }

    private val context: Context? 
        get() = manualContext ?: AndroidService.context

    override fun setAlarm(id: String, title: String, timeMillis: Long) {
        Log.d("Remmi", "[AndroidAlarmHandler] - [setAlarm] executed")
        val currentContext = context
        Log.d("AndroidAlarmHandler", "setAlarm called for $id ('$title') at $timeMillis. Context available: ${currentContext != null}")
        
        if (currentContext == null) {
            Log.e("AndroidAlarmHandler", "Abort setAlarm: Android context is null. Make sure MainActivity initialized it.")
            return
        }

        val now = System.currentTimeMillis()
        if (timeMillis <= now) {
            Log.w("AndroidAlarmHandler", "Warning: Scheduling alarm in the past! (Current: $now, Requested: $timeMillis). Difference: ${timeMillis - now}ms")
        }

        try {
            val alarmManager = currentContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Check for exact alarm permission on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.e("AndroidAlarmHandler", "Abort setAlarm: Missing SCHEDULE_EXACT_ALARM permission or user has disabled it in settings.")
                    // Optional: You might want to fire an intent to open settings here
                    return
                }
            }

            val intent = Intent(currentContext, AlarmReceiver::class.java).apply {
                putExtra("ALARM_ID", id)
                putExtra("ALARM_TITLE", title)
                // Add a unique action to help debugging and ensure distinct intents if needed
                action = "com.remmi.app.plugins.alarm.ACTION_TRIGGER_$id"
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                currentContext,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            Log.d("AndroidAlarmHandler", "Scheduling with AlarmManager.setAlarmClock for id: $id")
            val alarmClockInfo = AlarmManager.AlarmClockInfo(timeMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d("AndroidAlarmHandler", "System alarm scheduled successfully for $id")
        } catch (e: Exception) {
            Log.e("AndroidAlarmHandler", "CRITICAL: Failed to schedule system alarm for $id: ${e.message}", e)
        }
    }

    override fun syncToSystemClock(title: String, timeMillis: Long) {
        Log.d("Remmi", "[AndroidAlarmHandler] - [syncToSystemClock] executed")
        val currentContext = context ?: return
        Log.d("AndroidAlarmHandler", "syncToSystemClock called for '$title' at $timeMillis")

        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = timeMillis
        }

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, title)
            putExtra(AlarmClock.EXTRA_HOUR, calendar.get(java.util.Calendar.HOUR_OF_DAY))
            putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(java.util.Calendar.MINUTE))
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            currentContext.startActivity(intent)
            Log.d("AndroidAlarmHandler", "Sent intent to system clock app for '$title'")
        } catch (e: Exception) {
            Log.e("AndroidAlarmHandler", "Failed to sync to system clock app: ${e.message}")
        }
    }

    override fun fetchSystemAlarms(): List<AlarmItem> {
        Log.d("Remmi", "[AndroidAlarmHandler] - [fetchSystemAlarms] executed")
        val currentContext = context ?: return emptyList()
        val alarms = mutableListOf<AlarmItem>()
        
        // List of common Alarm Provider URIs
        val providers = listOf(
            "content://com.android.deskclock/alarm",
            "content://com.google.android.deskclock/alarm",
            "content://com.sec.android.app.clockpackage/alarm"
        )

        providers.forEach { uriString ->
            try {
                val uri = Uri.parse(uriString)
                val cursor = currentContext.contentResolver.query(uri, null, null, null, null)
                
                cursor?.use { c ->
                    val idIdx = c.getColumnIndex("_id")
                    val hourIdx = c.getColumnIndex("hour")
                    val minIdx = c.getColumnIndex("minutes")
                    val labelIdx = c.getColumnIndex("label")
                    val enabledIdx = c.getColumnIndex("enabled")

                    while (c.moveToNext()) {
                        val enabled = if (enabledIdx != -1) c.getInt(enabledIdx) == 1 else true
                        if (!enabled) continue

                        val hour = if (hourIdx != -1) c.getInt(hourIdx) else 0
                        val minutes = if (minIdx != -1) c.getInt(minIdx) else 0
                        val label = if (labelIdx != -1) c.getString(labelIdx) ?: "System Alarm" else "System Alarm"
                        val id = if (idIdx != -1) "system_${c.getInt(idIdx)}" else "system_${System.currentTimeMillis()}"

                        // Calculate next occurrence
                        val now = Calendar.getInstance()
                        val alarmTime = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minutes)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        if (alarmTime.before(now)) {
                            alarmTime.add(Calendar.DAY_OF_YEAR, 1)
                        }

                        alarms.add(
                            AlarmItem(
                                id = id,
                                created = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                                modified = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                                title = label,
                                description = "System Alarm",
                                time = Instant.fromEpochMilliseconds(alarmTime.timeInMillis),
                                isPriority = false
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("AndroidAlarmHandler", "Could not query provider $uriString: ${e.message}")
            }
        }
        
        return alarms
    }

    override fun openSystemAlarmApp() {
        Log.d("Remmi", "[AndroidAlarmHandler] - [openSystemAlarmApp] executed")
        val currentContext = context ?: return
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            currentContext.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AndroidAlarmHandler", "Failed to open system alarm app: ${e.message}")
        }
    }

    override fun cancelAlarm(id: String) {
        Log.d("Remmi", "[AndroidAlarmHandler] - [cancelAlarm] executed")
        val currentContext = context
        Log.d("AndroidAlarmHandler", "cancelAlarm called for $id. Context available: ${currentContext != null}")
        
        if (currentContext == null) {
            Log.e("AndroidAlarmHandler", "Abort cancelAlarm: Context is null.")
            return
        }

        try {
            val alarmManager = currentContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(currentContext, AlarmReceiver::class.java).apply {
                action = "com.remmi.app.plugins.alarm.ACTION_TRIGGER_$id"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                currentContext,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel() // Also cancel the pending intent itself
                Log.d("AndroidAlarmHandler", "System alarm canceled successfully: $id")
            } else {
                Log.d("AndroidAlarmHandler", "cancelAlarm: No existing PendingIntent found for $id, nothing to cancel.")
            }
        } catch (e: Exception) {
            Log.e("AndroidAlarmHandler", "Failed to cancel system alarm for $id: ${e.message}", e)
        }
    }

    override fun postNotification(title: String, content: String) {
        Log.d("Remmi", "[AndroidAlarmHandler] - [postNotification] executed")
        val currentContext = context
        Log.d("AndroidAlarmHandler", "postNotification called for '$title'. Context available: ${currentContext != null}")
        
        if (currentContext == null) {
            Log.e("AndroidAlarmHandler", "Abort postNotification: Context is null.")
            return
        }

        try {
            val notificationManager = currentContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "remmi_alarms"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Remmi Alarms",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(currentContext, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
            Log.d("AndroidAlarmHandler", "Notification posted successfully via NotificationManager.")
        } catch (e: Exception) {
            Log.e("AndroidAlarmHandler", "Failed to post notification: ${e.message}", e)
        }
    }

    // --- Other methods are placeholders for this plugin ---
    override fun fetchContacts(): List<Any> {
        Log.d("Remmi", "[AndroidAlarmHandler] - [fetchContacts] executed")
        return emptyList()
    }
    override fun createContact(name: String, phone: String) {
        Log.d("Remmi", "[AndroidAlarmHandler] - [createContact] executed")
    }
    override fun fetchCalendarEvents(): List<Any> {
        Log.d("Remmi", "[AndroidAlarmHandler] - [fetchCalendarEvents] executed")
        return emptyList()
    }
    override fun createCalendarEvent(title: String, timeMillis: Long) {
        Log.d("Remmi", "[AndroidAlarmHandler] - [createCalendarEvent] executed")
    }
    override fun initiatePhoneCall(phoneNumber: String) {
        Log.d("Remmi", "[AndroidAlarmHandler] - [initiatePhoneCall] executed")
    }
    override fun sendSmsMessage(phoneNumber: String, message: String) {
        Log.d("Remmi", "[AndroidAlarmHandler] - [sendSmsMessage] executed")
    }
    override fun requestCurrentLocation(onLocationResult: (lat: Double, lon: Double) -> Unit) {
        Log.d("Remmi", "[AndroidAlarmHandler] - [requestCurrentLocation] executed")
    }
}
