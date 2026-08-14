package com.remmi.app.plugins.tasks

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.plugins.widgets.RemmiWidget

/**
 * Dashboard widget for the Tasks plugin.
 */
class TasksWidget(
    private val tasksActions: TasksActions
) : RemmiWidget {

    init {
        Log.d("Remmi", "[TasksWidget] - [constructor] executed")
    }

    @Composable
    override fun Content() {
        Log.d("Remmi", "[TasksWidget] - [Content] executed")
        var todayTasks by remember { mutableStateOf(emptyList<TaskItem>()) }

        LaunchedEffect(Unit) {
            todayTasks = tasksActions.getTodayTasks()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "✅ Active Tasks (Today)",
                    style = MaterialTheme.typography.titleMedium
                )
                if (todayTasks.isEmpty()) {
                    Text("No active tasks for today", style = MaterialTheme.typography.bodySmall)
                } else {
                    todayTasks.forEach { task ->
                        Text(
                            text = "• ${task.title}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
