package com.remmi.app.core.service.android.implementations

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.remmi.app.core.service.android.*
import com.remmi.app.plugins.alarm.AlarmItem
import com.remmi.app.plugins.alarm.AlarmReceiver
import kotlinx.datetime.Instant
import java.util.Calendar

/**
 * ANDROID ALARM SERVICE
 *
 * Android-specific implementation for alarms and notifications.
 */
class AndroidAlarmService(private val context: Context) : AlarmService, NotificationService {


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[AndroidAlarmService] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    override fun setAlarm(id: String, title: String, timeMillis: Long, useSound: Boolean, useVibration: Boolean) {
        Log.d("Remmi", "[AndroidAlarmService] - [setAlarm] executed")
        
        val now = System.currentTimeMillis()
        if (timeMillis <= now) {
            Log.w("AndroidAlarmService", "Warning: Scheduling alarm in the past!")
        }

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.e("AndroidAlarmService", "Abort setAlarm: Missing SCHEDULE_EXACT_ALARM permission.")
                    return
                }
            }

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("ALARM_ID", id)
                putExtra("ALARM_TITLE", title)
                putExtra("USE_SOUND", useSound)
                putExtra("USE_VIBRATION", useVibration)
                action = "com.remmi.app.plugins.alarm.ACTION_TRIGGER_$id"
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmClockInfo = AlarmManager.AlarmClockInfo(timeMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d("AndroidAlarmService", "System alarm scheduled successfully for $id")
        } catch (e: Exception) {
            Log.e("AndroidAlarmService", "CRITICAL: Failed to schedule system alarm for $id: ${e.message}", e)
        }
    }

    override fun cancelAlarm(id: String) {
        Log.d("Remmi", "[AndroidAlarmService] - [cancelAlarm] executed")

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.remmi.app.plugins.alarm.ACTION_TRIGGER_$id"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d("AndroidAlarmService", "System alarm canceled successfully: $id")
            }
        } catch (e: Exception) {
            Log.e("AndroidAlarmService", "Failed to cancel system alarm for $id: ${e.message}", e)
        }
    }

    override fun syncToSystemClock(title: String, timeMillis: Long) {
        Log.d("Remmi", "[AndroidAlarmService] - [syncToSystemClock] executed")

        val calendar = Calendar.getInstance().apply {
            timeInMillis = timeMillis
        }

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, title)
            putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
            putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE))
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AndroidAlarmService", "Failed to sync to system clock app: ${e.message}")
        }
    }

    override fun removeFromSystemClock(title: String, timeMillis: Long) {
        Log.d("Remmi", "[AndroidAlarmService] - [removeFromSystemClock] executed")
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timeMillis
        }
        val intent = Intent(AlarmClock.ACTION_DISMISS_ALARM).apply {
            putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_TIME)
            putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
            putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE))
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AndroidAlarmService", "Failed to remove alarm from system clock app: ${e.message}")
        }
    }

    override fun fetchSystemAlarms(): List<AlarmItem> {
        Log.d("Remmi", "[AndroidAlarmService] - [fetchSystemAlarms] executed")
        val alarms = mutableListOf<AlarmItem>()
        
        val providers = listOf(
            "content://com.android.deskclock/alarm",
            "content://com.google.android.deskclock/alarm",
            "content://com.sec.android.app.clockpackage/alarm"
        )

        providers.forEach { uriString ->
            try {
                val uri = Uri.parse(uriString)
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                
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
                Log.w("AndroidAlarmService", "Could not query provider $uriString: ${e.message}")
            }
        }
        
        return alarms
    }

    override fun openSystemAlarmApp() {
        Log.d("Remmi", "[AndroidAlarmService] - [openSystemAlarmApp] executed")
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AndroidAlarmService", "Failed to open system alarm app: ${e.message}")
        }
    }

    override fun postNotification(title: String, content: String, useSound: Boolean, useVibration: Boolean) {
        Log.d("Remmi", "[AndroidAlarmService] - [postNotification] executed")

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "remmi_alarms"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Remmi Alarms",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableVibration(useVibration)
                    if (!useSound) {
                        setSound(null, null)
                    }
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            if (!useSound) {
                builder.setSound(null)
            }
            if (!useVibration) {
                builder.setVibrate(longArrayOf(0L))
            }

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            Log.e("AndroidAlarmService", "Failed to post notification: ${e.message}", e)
        }
    }
}
