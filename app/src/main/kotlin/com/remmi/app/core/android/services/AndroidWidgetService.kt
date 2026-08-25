package com.remmi.app.core.android.services

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.remmi.app.R
import com.remmi.app.plugins.dashboard.RemmiWidgetProvider

/**
 * ANDROID WIDGET SERVICE
 *
 * Wrapper for AppWidgetManager to restrict direct Context access.
 */
class AndroidWidgetService(private val context: Context) {

    fun refreshWidgets() {
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
