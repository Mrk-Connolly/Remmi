package com.remmi.app.core.automation

import com.remmi.app.core.automation.dailyBriefing.DailyBriefingSettings

/**
 * AUTOMATION SCHEDULER
 *
 * Interface for scheduling background automation tasks.
 */
interface AutomationScheduler {

    /**                                 Schedule Daily Briefing
     * Set up a recurring background trigger at the specified time.
     * */
    fun scheduleDailyBriefing(settings: DailyBriefingSettings)

    /**                                 Cancel Daily Briefing
     * Remove any existing briefing schedules.
     * */
    fun cancelDailyBriefing()
}
