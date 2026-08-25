package com.remmi.app.plugins.dashboard.logic

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.remmi.app.R
import com.remmi.app.core.events.events.*
import com.remmi.app.plugins.dashboard.RemmiWidgetProvider

/**
 * REMMI WIDGET UPDATE MANAGER
 * 
 * Listens for system events and triggers refreshes for the home-screen widget.
 */
class RemmiWidgetUpdateManager(private val context: Context) : EventListener {

    override suspend fun onEvent(event: RemmiEvent) {
        when (event) {
            is TaskCreatedEvent,
            is TaskUpdatedEvent,
            is TaskDeletedEvent,
            is CalendarEventCreatedEvent,
            is CalendarEventUpdatedEvent,
            is CalendarEventDeletedEvent -> {
                Log.d("Remmi", "[RemmiWidgetUpdateManager] - Data changed, updating widgets")
                refreshWidgets()
            }
        }
    }

    private fun refreshWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, RemmiWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.task_list)
        
        val updateIntent = android.content.Intent(context, RemmiWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(updateIntent)
    }
}
