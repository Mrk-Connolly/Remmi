package com.remmi.app.plugins.calendar

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.widgets.RemmiWidget
import com.remmi.app.plugins.tasks.TaskItem

/**
 * Dashboard widget for the Calendar plugin.
 */
class CalendarWidget(
    override val metadata: PluginMetadata,
    private val calendarActions: CalendarActions
) : RemmiWidget {

    init {
        Log.d("Remmi", "[CalendarWidget] - [constructor] executed")
    }

    @Composable
    override fun Content() {
        Log.d("Remmi", "[CalendarWidget] - [Content] executed")
        var todayEvents by remember { mutableStateOf(emptyList<CalendarItem>()) }
        var priorityTasks by remember { mutableStateOf(emptyList<TaskItem>()) }

        val tasksActions = remember { calendarActions.getTasksActions() }

        LaunchedEffect(Unit) {
            todayEvents = calendarActions.getTodayEvents()
            priorityTasks = tasksActions?.getHighPriorityTasksOfMonth() ?: emptyList()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📅 Today's Events",
                    style = MaterialTheme.typography.titleMedium
                )
                if (todayEvents.isEmpty()) {
                    Text("No events today", style = MaterialTheme.typography.bodySmall)
                } else {
                    todayEvents.forEach { event ->
                        Text("• ${event.title}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "🔥 High Priority Tasks (Month)",
                    style = MaterialTheme.typography.titleMedium
                )
                if (priorityTasks.isEmpty()) {
                    Text("No high priority tasks", style = MaterialTheme.typography.bodySmall)
                } else {
                    priorityTasks.forEach { task ->
                        Text("• ${task.title}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
