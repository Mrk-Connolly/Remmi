package com.remmi.app.core.screens

import androidx.compose.runtime.Composable
import com.remmi.app.core.RemmiClass

/**
 * Interface for full-screen UI views provided by plugins.
 *
 * Each plugin typically has one main screen that handles its primary user
 * interactions.
 */
interface RemmiScreen : RemmiClass {

    /**
     * The UI content of the screen, defined as a Composable function.
     */
    @Composable
    fun Content()
}
