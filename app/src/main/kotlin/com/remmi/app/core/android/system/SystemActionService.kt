package com.remmi.app.core.android.system

import com.remmi.app.core.eventBus.commands.CommandListener

/**
 * SYSTEM ACTION SERVICE
 *
 * Interface for triggering generic Android system actions.
 */
interface SystemActionService : CommandListener {
    fun openFile(filePath: String, mimeType: String = "*/*")
}
