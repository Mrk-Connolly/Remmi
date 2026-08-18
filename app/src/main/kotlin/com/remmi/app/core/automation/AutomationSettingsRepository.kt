package com.remmi.app.core.automation

import android.content.Context
import android.util.Log

/**
 * AUTOMATION SETTINGS REPOSITORY
 *
 * Manages persistent storage for automation-related settings.
 * Uses SharedPreferences for local persistence.
 */
class AutomationSettingsRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("remmi_automation_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BRIEFING_ENABLED = "daily_briefing_enabled"
        private const val KEY_BRIEFING_HOUR = "daily_briefing_hour"
        private const val KEY_BRIEFING_MINUTE = "daily_briefing_minute"
    }

    /**                                 Get Briefing Settings
     * Retrieve the current daily briefing configuration.
     * */
    fun getBriefingSettings(): DailyBriefingSettings {
        return DailyBriefingSettings(
            enabled = prefs.getBoolean(KEY_BRIEFING_ENABLED, false),
            hour = prefs.getInt(KEY_BRIEFING_HOUR, 7),
            minute = prefs.getInt(KEY_BRIEFING_MINUTE, 0)
        )
    }

    /**                                 Update Briefing Settings
     * Persist new briefing settings.
     * */
    fun updateBriefingSettings(settings: DailyBriefingSettings) {
        Log.d("Remmi", "[AutomationSettingsRepository] - Updating settings: $settings")
        prefs.edit()
            .putBoolean(KEY_BRIEFING_ENABLED, settings.enabled)
            .putInt(KEY_BRIEFING_HOUR, settings.hour)
            .putInt(KEY_BRIEFING_MINUTE, settings.minute)
            .apply()
    }
}
