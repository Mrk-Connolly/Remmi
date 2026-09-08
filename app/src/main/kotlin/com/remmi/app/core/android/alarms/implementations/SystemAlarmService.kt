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
import com.remmi.app.plugins.alarm.AlarmReceiver
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
                Log.i("Remmi", "[SystemAlarmService] - Setting internal alarm: ${command.id}")
                setAlarm(command.id, command.title, command.timeMillis, command.useSound, command.useVibration)
            }
            is CancelSystemAlarmCommand -> {
                Log.i("Remmi", "[SystemAlarmService] - Canceling internal alarm: ${command.id}")
                cancelAlarm(command.id)
            }
            is SyncSystemClockCommand -> {
                Log.i("Remmi", "[SystemAlarmService] - Syncing to system clock app: ${command.title}")
                syncToSystemClockExtended(command)
            }
            is RemoveSystemClockCommand -> {
                Log.i("Remmi", "[SystemAlarmService] - Removing from system clock app: ${command.title}")
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
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w("SystemAlarmService", "Exact alarms not allowed. Falling back to inexact.")
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
                context, id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmClockInfo = AlarmManager.AlarmClockInfo(timeMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: Exception) {
            Log.e("SystemAlarmService", "Failed to schedule internal alarm: ${e.message}")
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
            Log.e("SystemAlarmService", "Failed to cancel internal alarm: ${e.message}")
        }
    }

    private fun syncToSystemClockExtended(command: SyncSystemClockCommand) {
        val calendar = Calendar.getInstance().apply { timeInMillis = command.timeMillis }
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, command.title)
            putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
            putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE))
            putExtra(AlarmClock.EXTRA_VIBRATE, command.vibrate)
            putExtra(AlarmClock.EXTRA_SKIP_UI, command.skipUi)
            
            command.days?.let { remmiDays ->
                val androidDays = remmiDays.map { remmiDay ->
                    // Remmi: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
                    // Android Calendar: 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
                    if (remmiDay == 7) Calendar.SUNDAY else remmiDay + 1
                }
                putExtra(AlarmClock.EXTRA_DAYS, ArrayList(androidDays))
            }
            
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { 
            context.startActivity(intent) 
        } catch (e: Exception) {
            Log.e("SystemAlarmService", "Failed to set alarm in system clock app: ${e.message}")
        }
    }

    override fun syncToSystemClock(title: String, timeMillis: Long) {
        // Fallback for simple calls
        syncToSystemClockExtended(SyncSystemClockCommand(title, timeMillis))
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
        try { 
            context.startActivity(intent) 
        } catch (e: Exception) {
             Log.e("SystemAlarmService", "Failed to dismiss alarm in system clock app: ${e.message}")
        }
    }

    override fun getNextSystemAlarm(): Long? {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.nextAlarmClock?.triggerTime
    }

    override fun openSystemAlarmApp() {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply { 
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) 
        }
        try { 
            context.startActivity(intent) 
        } catch (e: Exception) {
            Log.e("SystemAlarmService", "Failed to open system clock app: ${e.message}")
        }
    }
}
