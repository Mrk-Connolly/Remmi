package com.remmi.app.core.automation

import android.util.Log
import com.remmi.app.core.automation.features.dailybriefing.DailyBriefingSettings
import com.remmi.app.core.android.services.SystemSettingsService

/**
 * AUTOMATION SETTINGS REPOSITORY
 *
 * Manages persistent storage for automation-related settings.
 * Uses SystemSettingsService for local persistence.
 */
class AutomationSettingsRepository(private val settingsService: SystemSettingsService) {

    companion object {
        private const val KEY_BRIEFING_ENABLED = "daily_briefing_enabled"
        private const val KEY_BRIEFING_HOUR = "daily_briefing_hour"
        private const val KEY_BRIEFING_MINUTE = "daily_briefing_minute"
        private const val KEY_LOCK_SCREEN_SUMMARY_ENABLED = "lock_screen_summary_enabled"
    }

    /**                                 Get Briefing Settings
     * Retrieve the current daily briefing configuration.
     * */
    fun getBriefingSettings(): DailyBriefingSettings {
        return DailyBriefingSettings(
            enabled = settingsService.getBoolean(KEY_BRIEFING_ENABLED, false),
            hour = settingsService.getInt(KEY_BRIEFING_HOUR, 7),
            minute = settingsService.getInt(KEY_BRIEFING_MINUTE, 0)
        )
    }

    /**                                 Update Briefing Settings
     * Persist new briefing settings.
     * */
    fun updateBriefingSettings(settings: DailyBriefingSettings) {
        Log.d("Remmi", "[AutomationSettingsRepository] - Updating settings: $settings")
        settingsService.setBoolean(KEY_BRIEFING_ENABLED, settings.enabled)
        settingsService.setInt(KEY_BRIEFING_HOUR, settings.hour)
        settingsService.setInt(KEY_BRIEFING_MINUTE, settings.minute)
    }

    /**                                 Get Lock Screen Summary Enabled
     * Check if the persistent lock screen notification is active.
     * */
    fun isLockScreenSummaryEnabled(): Boolean {
        return settingsService.getBoolean(KEY_LOCK_SCREEN_SUMMARY_ENABLED, true) // Default to true
    }

    /**                                 Update Lock Screen Summary Enabled
     * Toggle the persistent lock screen notification.
     * */
    fun setLockScreenSummaryEnabled(enabled: Boolean) {
        settingsService.setBoolean(KEY_LOCK_SCREEN_SUMMARY_ENABLED, enabled)
    }
}
