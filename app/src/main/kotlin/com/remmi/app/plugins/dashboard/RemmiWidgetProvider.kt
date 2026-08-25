package com.remmi.app.plugins.dashboard

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import com.remmi.app.MainActivity
import com.remmi.app.R
import com.remmi.app.RemmiApplication
import com.remmi.app.core.events.commands.UpdateTaskCommand
import com.remmi.app.core.model.tasks.TaskItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * REMMI WIDGET PROVIDER
 * 
 * Handles widget lifecycle, updates, and user interactions.
 */
class RemmiWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_COMPLETE_TASK = "com.remmi.app.action.COMPLETE_TASK"
        const val EXTRA_TASK_ID = "com.remmi.app.extra.TASK_ID"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_COMPLETE_TASK) {
            val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
            Log.d("Remmi", "[RemmiWidgetProvider] - Received completion for task: $taskId")
            
            val app = context.applicationContext as RemmiApplication
            val host = app.remmiHost
            
            CoroutineScope(Dispatchers.IO).launch {
                host.start() // Ensure system is started
                
                // We'll use a generic "ToggleTask" or "CompleteTask" via EventBus
                // This removes the need for TasksPlugin direct dependency here.
                // We need to fetch the task first to toggle it, or have a specific "MarkCompleted" command.
                
                // For now, I'll send a dummy update if I don't have the task object.
                // Ideally we have a MarkTaskCompletedCommand(taskId).
                
                Log.d("Remmi", "[RemmiWidgetProvider] - Executing ToggleTask via EventBus for $taskId")
                host.runtime.eventBus.publishCommand(com.remmi.app.core.events.commands.ToggleTaskCommand(taskId))
            }
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_remmi_today)

        // Setup the list service
        val intent = Intent(context, RemmiWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        views.setRemoteAdapter(R.id.task_list, intent)
        views.setEmptyView(R.id.task_list, R.id.empty_view)

        // Setup Task Completion Intent Template
        val completeIntent = Intent(context, RemmiWidgetProvider::class.java).apply {
            action = ACTION_COMPLETE_TASK
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context, 0, completeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setPendingIntentTemplate(R.id.task_list, completePendingIntent)

        // Setup App Opening Intent
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.header, openAppPendingIntent)

        // Setup Refresh Button
        val refreshIntent = Intent(context, RemmiWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context, appWidgetId, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
