package com.remmi.app.plugins.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.remmi.app.core.widgets.RemmiWidget

/**
 * Dashboard widget for the Tasks plugin.
 *
 * Displays a quick overview of the most recent tasks.
 */
class TasksWidget(private val actions: TasksActions) : RemmiWidget {

    @Composable
    override fun Content() {
        var tasks by remember { mutableStateOf(emptyList<TaskItem>()) }

        LaunchedEffect(Unit) {
            tasks = actions.getAllTasks().take(3) // Show top 3
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "✅ Recent Tasks",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                if (tasks.isEmpty()) {
                    Text("All caught up!", style = MaterialTheme.typography.bodySmall)
                } else {
                    tasks.forEach { task ->
                        Text(
                            text = "• ${task.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }
                }
            }
        }
    }
}
