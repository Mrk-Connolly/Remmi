package com.remmi.app.plugins.dashboard.logic

import android.util.Log
import com.remmi.app.core.eventBus.events.*
import com.remmi.app.core.android.services.AndroidWidgetService

/**
 * REMMI WIDGET UPDATE MANAGER
 * 
 * Listens for system events and triggers refreshes for the home-screen widget.
 */
class RemmiWidgetUpdateManager(private val widgetService: AndroidWidgetService) : EventListener {

    override suspend fun onEvent(event: RemmiEvent) {
        when (event) {
            is TaskCreatedEvent,
            is TaskUpdatedEvent,
            is TaskDeletedEvent,
            is CalendarEventCreatedEvent,
            is CalendarEventUpdatedEvent,
            is CalendarEventDeletedEvent -> {
                Log.d("Remmi", "[RemmiWidgetUpdateManager] - Data changed, updating widgets")
                widgetService.refreshWidgets()
            }
        }
    }
}
