package com.remmi.app.core.widgets

import androidx.compose.runtime.Composable

/**
 * Interface for dashboard widgets provided by plugins.
 */
interface RemmiWidget {

    /**
     * The UI content of the widget, defined as a Composable function.
     */
    @Composable
    fun Content()

    // To Do
    fun Refresh() {}

    // To Do
    fun Disable() {}


}
