package com.remmi.app.core.automation.features.dailybriefing

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.remmi.app.core.eventBus.commands.RunDailyBriefingCommand
import com.remmi.app.core.host.RemmiHost
import kotlinx.coroutines.delay

/**
 * DAILY BRIEFING WORKER
 *
 * Background worker that executes the daily briefing logic.
 * It initializes a headless RemmiHost and triggers the AutomationEngine via EventBus.
 */
class DailyBriefingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("Remmi", "[DailyBriefingWorker] - Starting background work")

        return try {
            // 1. Initialize Headless Runtime
            val host = RemmiHost(applicationContext)
            host.start()
            
            // 2. Trigger Briefing Command
            Log.d("Remmi", "[DailyBriefingWorker] - Publishing RunDailyBriefingCommand")
            host.runtime.eventBus.publishCommand(RunDailyBriefingCommand())
            
            // 3. Give automation engine time to process (gather data and notify)
            // In a more complex system, we might wait for a completion event.
            delay(10000) 
            
            Log.d("Remmi", "[DailyBriefingWorker] - Work completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("Remmi", "[DailyBriefingWorker] - Failure: ${e.message}", e)
            Result.failure()
        }
    }
}
