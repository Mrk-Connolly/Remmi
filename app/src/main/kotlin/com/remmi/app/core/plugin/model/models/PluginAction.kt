package com.remmi.app.core.plugin.model.models

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * PLUGIN ACTION
 *
 * Base contract for a generic plugin action that can be displayed and triggered
 * from another plugin's UI (e.g., a "Create Alarm" button in the Calendar).
 */
interface PluginAction {
    val id: String
    val pluginId: String
    val title: String
    val icon: ImageVector
    val description: String? get() = null

    /**
     * Launch the interaction for this action.
     * Implementations typically open a popup or request data from the user.
     */
    fun launch()
}
