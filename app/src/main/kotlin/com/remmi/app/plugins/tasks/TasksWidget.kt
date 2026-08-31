package com.remmi.app.plugins.tasks

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.plugins.tasks.models.TaskItem
import com.remmi.app.core.ui.RemmiCard
import kotlinx.coroutines.launch

/**
 * Dashboard widget for the Tasks plugin.
 */
class TasksWidget(
    override val metadata: PluginMetadata,
    private val tasksActions: TasksActions
) : RemmiWidget {

    init {
        Log.d("Remmi", "[TasksWidget] - [constructor] executed")
    }

    @Composable
    override fun Content() {
        Log.d("Remmi", "[TasksWidget] - [Content] executed")
        val scope = rememberCoroutineScope()
        var todayTasks by remember { mutableStateOf(emptyList<TaskItem>()) }

        val refreshTasks = {
            scope.launch {
                todayTasks = tasksActions.getTodayTasks()
            }
        }

        LaunchedEffect(Unit) {
            refreshTasks()
        }

        com.remmi.app.core.ui.RemmiCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Active Tasks",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(Modifier.height(16.dp))

                if (todayTasks.isEmpty()) {
                    Text(
                        "You're all caught up!", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        todayTasks.take(3).forEach { task ->
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                // Square Checkbox
                                Surface(
                                    onClick = {
                                        scope.launch {
                                            tasksActions.toggleTask(task)
                                            refreshTasks()
                                        }
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (task.completed) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    border = BorderStroke(1.dp, if (task.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    if (task.completed) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(2.dp)
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    textDecoration = if (task.completed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                    color = if (task.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        if (todayTasks.size > 3) {
                            Text(
                                "+ ${todayTasks.size - 3} more tasks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
