package com.remmi.app.core.automation

/**
 * DAILY BRIEFING SETTINGS
 *
 * Configuration for the daily briefing automation.
 */
data class DailyBriefingSettings(
    val enabled: Boolean = false,
    val hour: Int = 7,
    val minute: Int = 0
)
