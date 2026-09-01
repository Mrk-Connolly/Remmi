package com.remmi.app.core.android.alarms.implementations

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.util.Log
import com.remmi.app.core.android.alarms.AlarmService
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.plugins.alarm.models.AlarmItem
import com.remmi.app.plugins.alarm.AlarmReceiver
import kotlinx.datetime.Instant
import java.util.Calendar

/**
 * SYSTEM ALARM SERVICE
 * 
 * Android-specific implementation for scheduling and managing system alarms.
 */
class SystemAlarmService(private val context: Context) : AlarmService {

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is SetSystemAlarmCommand -> {
                Log.i("Remmi", "[SystemAlarmService] - Setting system alarm: ${command.id}")
                setAlarm(command.id, command.title, command.timeMillis, command.useSound, command.useVibration)
            }
            is CancelSystemAlarmCommand -> {
                Log.i("Remmi", "[SystemAlarmService] - Canceling system alarm: ${command.id}")
                cancelAlarm(command.id)
            }
            is SyncSystemClockCommand -> {
                Log.i("Remmi", "[SystemAlarmService] - Syncing to system clock: ${command.title}")
                syncToSystemClock(command.title, command.timeMillis)
            }
            is RemoveSystemClockCommand -> {
                Log.i("Remmi", "[SystemAlarmService] - Removing from system clock: ${command.title}")
                removeFromSystemClock(command.title, command.timeMillis)
            }
            is OpenSystemAlarmAppCommand -> {
                Log.i("Remmi", "[SystemAlarmService] - Opening system alarm app")
                openSystemAlarmApp()
            }
        }
    }

    override fun setAlarm(id: String, title: String, timeMillis: Long, useSound: Boolean, useVibration: Boolean) {
        val now = System.currentTimeMillis()
        if (timeMillis <= now) return

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) return
            }

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("ALARM_ID", id)
                putExtra("ALARM_TITLE", title)
                putExtra("USE_SOUND", useSound)
                putExtra("USE_VIBRATION", useVibration)
                action = "com.remmi.app.plugins.alarm.ACTION_TRIGGER_$id"
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context, id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmClockInfo = AlarmManager.AlarmClockInfo(timeMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: Exception) {
            Log.e("SystemAlarmService", "Failed to schedule alarm: ${e.message}")
        }
    }

    override fun cancelAlarm(id: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.remmi.app.plugins.alarm.ACTION_TRIGGER_$id"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, id.hashCode(), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        } catch (e: Exception) {
            Log.e("SystemAlarmService", "Failed to cancel alarm: ${e.message}")
        }
    }

    override fun syncToSystemClock(title: String, timeMillis: Long) {
        val calendar = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, title)
            putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
            putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE))
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { context.startActivity(intent) } catch (e: Exception) {}
    }

    override fun removeFromSystemClock(title: String, timeMillis: Long) {
        val intent = Intent(AlarmClock.ACTION_DISMISS_ALARM).apply {
            putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_TIME)
            val calendar = Calendar.getInstance().apply { timeInMillis = timeMillis }
            putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
            putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE))
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { context.startActivity(intent) } catch (e: Exception) {}
    }

    override fun fetchSystemAlarms(): List<AlarmItem> {
        return emptyList() 
    }

    override fun openSystemAlarmApp() {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        try { context.startActivity(intent) } catch (e: Exception) {}
    }
}
