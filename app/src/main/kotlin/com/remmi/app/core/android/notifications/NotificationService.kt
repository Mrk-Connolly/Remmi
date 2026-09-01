package com.remmi.app.core.android.notifications

import com.remmi.app.core.eventBus.commands.CommandListener

/**
 * NOTIFICATION SERVICE
 *
 * Interface for standard notification operations.
 */
interface NotificationService : CommandListener {

    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Post Notification
     * Display a system notification.
     * */
    fun postNotification(
        title: String,
        content: String,
        useSound: Boolean = true,
        useVibration: Boolean = true,
        tag: String? = null,
        ongoing: Boolean = false
    )

    /**                                 Post Live Update
     * Display a progress-centric notification (Android 16+).
     */
    fun postLiveUpdate(
        title: String,
        content: String,
        progress: Int,
        maxProgress: Int,
        tag: String?
    )
}
