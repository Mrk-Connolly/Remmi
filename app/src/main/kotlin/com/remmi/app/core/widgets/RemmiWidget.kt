package com.remmi.app.core.widgets

import androidx.compose.runtime.Composable
import com.remmi.app.core.RemmiClass

/**
 * Interface for dashboard widgets provided by plugins.
 *
 * Widgets are small, interactive components displayed on the main dashboard
 * to provide quick information or controls.
 */
interface RemmiWidget : RemmiClass {

    /**
     * The UI content of the widget, defined as a Composable function.
     */
    @Composable
    fun Content()
}
