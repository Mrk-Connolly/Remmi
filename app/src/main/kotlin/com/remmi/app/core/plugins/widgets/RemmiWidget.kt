package com.remmi.app.core.plugins.widgets

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginMetadata

/**
 * Interface for dashboard widgets provided by plugins.
 */
interface RemmiWidget {

    /**
     * Metadata describing the plugin. Used to determine widget enablement.
     */
    val metadata: PluginMetadata

    /**
     * Checks if the widget is currently enabled based on plugin settings.
     */
    fun isEnabled(): Boolean {
        return metadata.enabled && metadata.showWidget
    }

    /**
     * The UI content of the widget, defined as a Composable function.
     */
    @Composable
    fun Content()

}
