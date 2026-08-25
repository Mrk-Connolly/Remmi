package com.remmi.app.plugins.dashboard

import android.content.Intent
import android.widget.RemoteViewsService

/**
 * REMMI WIDGET SERVICE
 * 
 * Provides the RemoteViewsFactory for the task list.
 */
class RemmiWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return RemmiWidgetFactory(applicationContext, intent)
    }
}
