package com.remmi.app.core.service.android

/**
 * NOTIFICATION SERVICE
 *
 * Interface for standard notification operations.
 */
interface NotificationService {

    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Post Notification
     * Display a system notification.
     * */
    fun postNotification(title: String, content: String)
}
