package com.remmi.app.ui.screens.launcher.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.plugins.calendar.CalendarPlugin
import com.remmi.app.plugins.shopping_list.ShoppingListPlugin
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

        // Live clock ticker
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
        val shoppingPlugin = controller.pluginManager.plugins["shopping_list"] as? ShoppingListPlugin

        val calendarItems by calendarPlugin?.repository?.asFlow()?.collectAsState(initial = emptyList())
            ?: remember { mutableStateOf(emptyList()) }
        val taskItems by tasksPlugin?.repository?.asFlow()?.collectAsState(initial = emptyList())
            ?: remember { mutableStateOf(emptyList()) }
        val shoppingItems by shoppingPlugin?.repository?.asFlow()?.collectAsState(initial = emptyList())
            ?: remember { mutableStateOf(emptyList()) }

        val todayTasksCount = taskItems.count { !it.completed }
        val eventsCount = calendarItems.size
        val shoppingCount = shoppingItems.count { !it.completed }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // Live Clock Header
            Text(
                text = currentTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = currentDate.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Glance Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryChip(
                    count = eventsCount,
                    label = "Events",
                    modifier = Modifier.weight(1f)
                )
                SummaryChip(
                    count = todayTasksCount,
                    label = "Tasks",
                    modifier = Modifier.weight(1f)
                )
                SummaryChip(
                    count = shoppingCount,
                    label = "Shopping",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryChip(
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
