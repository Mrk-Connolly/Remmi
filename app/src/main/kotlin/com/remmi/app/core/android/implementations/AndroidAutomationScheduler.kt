package com.remmi.app.core.android.implementations

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.remmi.app.core.automation.AutomationScheduler
import com.remmi.app.core.automation.DailyBriefingSettings
import com.remmi.app.core.automation.DailyBriefingReceiver
import java.util.Calendar

/**
 * ANDROID AUTOMATION SCHEDULER
 *
 * Implementation of AutomationScheduler using Android's AlarmManager.
 */
class AndroidAutomationScheduler(private val context: Context) : AutomationScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val REQUEST_CODE_BRIEFING = 2001
    }

    override fun scheduleDailyBriefing(settings: DailyBriefingSettings) {
        Log.d("Remmi", "[AndroidAutomationScheduler] - Scheduling daily briefing at ${settings.hour}:${settings.minute}")

        val intent = Intent(context, DailyBriefingReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BRIEFING,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settings.hour)
            set(Calendar.MINUTE, settings.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If time is in the past, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Use setExactAndAllowWhileIdle for reliable trigger in Doze mode
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            Log.e("Remmi", "[AndroidAutomationScheduler] - Failed to set exact alarm: ${e.message}")
            // Fallback for devices where exact alarm permission is denied
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    override fun cancelDailyBriefing() {
        Log.d("Remmi", "[AndroidAutomationScheduler] - Canceling daily briefing")
        val intent = Intent(context, DailyBriefingReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BRIEFING,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
