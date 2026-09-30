package com.remmi.app.ui.screens.launcher.modules

import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController

/**
 * Supported types of launcher dashboard modules.
 */
enum class LauncherModuleType(val defaultTitle: String) {
    TODAY("Today"),
    CALENDAR("Upcoming Calendar"),
    TASKS("Tasks & Reminders"),
    SHOPPING("Shopping List"),
    QUICK_ACTIONS("Quick Actions"),
    CONTACTS("Favorite Contacts"),
    APP_WIDGETS("Android App Widgets")
}

/**
 * Configuration model for a launcher dashboard module.
 */
data class LauncherModuleConfig(
    val type: LauncherModuleType,
    val enabled: Boolean = true,
    val priority: Int = 0,
    val isExpanded: Boolean = true
)

/**
 * Interface that all launcher dashboard modules must implement.
 * Provides modularity, standardized rendering, and loose coupling with plugins.
 */
interface LauncherModule {
    val id: String
    val title: String
    val type: LauncherModuleType
    val priority: Int

    @Composable
    fun Content(
        controller: RemmiController,
        onNavigateToPlugin: (String) -> Unit
    )
}
