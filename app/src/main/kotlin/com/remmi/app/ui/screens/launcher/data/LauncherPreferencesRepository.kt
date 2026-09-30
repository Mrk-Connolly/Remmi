package com.remmi.app.ui.screens.launcher.data

import android.content.Context
import android.content.SharedPreferences
import com.remmi.app.ui.screens.launcher.modules.LauncherModuleConfig
import com.remmi.app.ui.screens.launcher.modules.LauncherModuleType

/**
 * Repository for persisting launcher configuration, gesture preferences, and module ordering.
 */
class LauncherPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("remmi_launcher_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MODULES_ORDER = "modules_order"
        private const val KEY_SWIPE_UP_ACTION = "swipe_up_action" // "APP_DRAWER" or "SEARCH"
        private const val KEY_SWIPE_DOWN_ACTION = "swipe_down_action" // "NOTIFICATIONS" or "SEARCH"
        private const val KEY_SHOW_CLOCK = "show_clock"
        private const val KEY_HIGH_DENSITY = "high_density"
        private const val KEY_WIDGET_IDS = "widget_ids"
    }

    /**
     * Get module configurations sorted by priority.
     */
    fun getModuleConfigs(): List<LauncherModuleConfig> {
        val savedString = prefs.getString(KEY_MODULES_ORDER, null)
        if (savedString.isNullOrBlank()) {
            return getDefaultModuleConfigs()
        }

        return try {
            // Format: "TODAY:1:0,CALENDAR:1:1,TASKS:1:2,SHOPPING:1:3,QUICK_ACTIONS:1:4,CONTACTS:1:5,APP_WIDGETS:1:6"
            savedString.split(",").mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size >= 3) {
                    val type = LauncherModuleType.entries.firstOrNull { it.name == parts[0] } ?: return@mapNotNull null
                    val enabled = parts[1] == "1"
                    val priority = parts[2].toIntOrNull() ?: 0
                    LauncherModuleConfig(type = type, enabled = enabled, priority = priority)
                } else null
            }.sortedBy { it.priority }
        } catch (e: Exception) {
            getDefaultModuleConfigs()
        }
    }

    /**
     * Save updated list of module configurations.
     */
    fun saveModuleConfigs(configs: List<LauncherModuleConfig>) {
        val serialized = configs.sortedBy { it.priority }.joinToString(",") {
            "${it.type.name}:${if (it.enabled) 1 else 0}:${it.priority}"
        }
        prefs.edit().putString(KEY_MODULES_ORDER, serialized).apply()
    }

    /**
     * Get default module configurations.
     */
    fun getDefaultModuleConfigs(): List<LauncherModuleConfig> {
        return LauncherModuleType.entries.mapIndexed { index, type ->
            LauncherModuleConfig(type = type, enabled = true, priority = index)
        }
    }

    var isSwipeUpAppDrawer: Boolean
        get() = prefs.getString(KEY_SWIPE_UP_ACTION, "APP_DRAWER") == "APP_DRAWER"
        set(value) {
            prefs.edit().putString(KEY_SWIPE_UP_ACTION, if (value) "APP_DRAWER" else "SEARCH").apply()
        }

    var isSwipeDownNotifications: Boolean
        get() = prefs.getString(KEY_SWIPE_DOWN_ACTION, "NOTIFICATIONS") == "NOTIFICATIONS"
        set(value) {
            prefs.edit().putString(KEY_SWIPE_DOWN_ACTION, if (value) "NOTIFICATIONS" else "SEARCH").apply()
        }

    var showClockHeader: Boolean
        get() = prefs.getBoolean(KEY_SHOW_CLOCK, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SHOW_CLOCK, value).apply()
        }

    var isHighDensity: Boolean
        get() = prefs.getBoolean(KEY_HIGH_DENSITY, false)
        set(value) {
            prefs.edit().putBoolean(KEY_HIGH_DENSITY, value).apply()
        }

    fun getWidgetIds(): List<Int> {
        val stringSet = prefs.getStringSet(KEY_WIDGET_IDS, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }
    }

    fun saveWidgetIds(ids: List<Int>) {
        val stringSet = ids.map { it.toString() }.toSet()
        prefs.edit().putStringSet(KEY_WIDGET_IDS, stringSet).apply()
    }

    fun addWidgetId(id: Int) {
        val current = getWidgetIds().toMutableList()
        if (!current.contains(id)) {
            current.add(id)
            saveWidgetIds(current)
        }
    }

    fun removeWidgetId(id: Int) {
        val current = getWidgetIds().toMutableList()
        current.remove(id)
        saveWidgetIds(current)
    }
}
