package com.remmi.app.ui.screens.launcher.modules

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.plugins.calendar.CalendarPlugin
import com.remmi.app.plugins.tasks.TasksPlugin
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class TodayModule : LauncherModule {
    override val id: String = "today_module"
    override val title: String = "Today"
    override val type: LauncherModuleType = LauncherModuleType.TODAY
    override val priority: Int = 0

    @Composable
    override fun Content(
        controller: RemmiController,
        onNavigateToPlugin: (String) -> Unit
    ) {
        var currentTime by remember { mutableStateOf("") }
        var currentDate by remember { mutableStateOf("") }

        // Live clock timer
        LaunchedEffect(Unit) {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            while (true) {
                val now = Date()
                currentTime = timeFormat.format(now)
                currentDate = dateFormat.format(now)
                delay(1000)
            }
        }

        // Gather statistics from existing plugins
        val calendarPlugin = controller.pluginManager.plugins["calendar"] as? CalendarPlugin
        val tasksPlugin = controller.pluginManager.plugins["tasks"] as? TasksPlugin

        val calendarItems by calendarPlugin?.repository?.asFlow()?.collectAsState(initial = emptyList())
            ?: remember { mutableStateOf(emptyList()) }
        val taskItems by tasksPlugin?.repository?.asFlow()?.collectAsState(initial = emptyList())
            ?: remember { mutableStateOf(emptyList()) }

        val todayTasksCount = taskItems.count { !it.completed }
        val eventsCount = calendarItems.size

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = currentTime,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-1).sp
            )
            Text(
                text = currentDate,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$eventsCount Events • $todayTasksCount Tasks Pending",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
