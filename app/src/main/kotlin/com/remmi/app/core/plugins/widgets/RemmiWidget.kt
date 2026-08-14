package com.remmi.app.core.plugins.widgets

import android.util.Log
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
    fun Refresh() {
        Log.d("Remmi", "[RemmiWidget] - [Refresh] executed")
    }

    // To Do
    fun Disable() {
        Log.d("Remmi", "[RemmiWidget] - [Disable] executed")
    }


}
