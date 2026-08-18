package com.remmi.app.core.automation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.remmi.app.core.service.android.implementations.AndroidAutomationScheduler

/**
 * DAILY BRIEFING RECEIVER
 *
 * Handles AlarmManager triggers and device reboots.
 */
class DailyBriefingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Remmi", "[DailyBriefingReceiver] - Received intent: ${intent.action}")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule after reboot
            reschedule(context)
        } else {
            // Triggered by AlarmManager for briefing
            triggerBriefing(context)
        }
    }

    private fun triggerBriefing(context: Context) {
        Log.d("Remmi", "[DailyBriefingReceiver] - Enqueuing briefing worker")
        val workRequest = OneTimeWorkRequestBuilder<DailyBriefingWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun reschedule(context: Context) {
        val repository = AutomationSettingsRepository(context)
        val settings = repository.getBriefingSettings()
        
        if (settings.enabled) {
            Log.d("Remmi", "[DailyBriefingReceiver] - Rescheduling daily briefing")
            val scheduler = AndroidAutomationScheduler(context)
            scheduler.scheduleDailyBriefing(settings)
        }
    }
}
