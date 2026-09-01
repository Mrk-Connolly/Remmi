package com.remmi.app.plugins.dashboard

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.remmi.app.R
import com.remmi.app.ui.RemmiApplication
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.calendar.CalendarPlugin
import com.remmi.app.plugins.tasks.models.TaskItem
import com.remmi.app.plugins.tasks.TasksPlugin
import kotlinx.coroutines.runBlocking

/**
 * REMMI WIDGET FACTORY
 * 
 * Fetches and manages data for the task list in the home-screen widget.
 */
class RemmiWidgetFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    private var tasks: List<TaskItem> = emptyList()
    private var nextEvent: CalendarItem? = null

    override fun onCreate() {}

    override fun onDataSetChanged() = runBlocking {
        Log.d("Remmi", "[RemmiWidgetFactory] - Fetching data for widget $appWidgetId")
        
        val app = context.applicationContext as RemmiApplication
        val host = app.remmiHost
        
        host.start()
        
        val tasksPlugin = host.runtime.pluginManager.plugins["tasks"] as? TasksPlugin
        val calendarPlugin = host.runtime.pluginManager.plugins["calendar"] as? CalendarPlugin
        
        tasks = tasksPlugin?.actions?.getTodayTasks() ?: emptyList()
        nextEvent = calendarPlugin?.actions?.getUpcomingEvents()?.firstOrNull()
    }

    override fun onDestroy() {
        tasks = emptyList()
    }

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= tasks.size) return RemoteViews(context.packageName, R.layout.widget_task_item)
        
        val task = tasks[position]
        val views = RemoteViews(context.packageName, R.layout.widget_task_item)
        
        views.setTextViewText(R.id.task_title, task.title)
        
        val fillInIntent = Intent().apply {
            putExtra(RemmiWidgetProvider.EXTRA_TASK_ID, task.id)
        }
        views.setOnClickFillInIntent(R.id.btn_complete, fillInIntent)
        
        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
