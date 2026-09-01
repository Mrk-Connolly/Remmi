package com.remmi.app.plugins.calendar

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.ui.components.RemmiCard

/**
 * Dashboard widget for the Calendar plugin.
 */
class CalendarWidget(
    override val metadata: PluginMetadata,
    private val calendarActions: CalendarActions
) : RemmiWidget {

    init {
        Log.d("Remmi", "[CalendarWidget] - Constructor initialized")
    }

    @Composable
    override fun Content() {
        Log.d("Remmi", "[CalendarWidget] - [Content] executed")
        var todayEvents by remember { mutableStateOf(emptyList<CalendarItem>()) }

        LaunchedEffect(Unit) {
            todayEvents = calendarActions.getTodayEvents()
        }

        RemmiCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Today's Events",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    IconButton(
                        onClick = {
                            // This usually triggers navigation to the calendar screen with editor open
                            // For now, we'll assume the dashboard container handles the navigation via the box clickable
                            // but we can also emit a specific command if needed.
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Event",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                if (todayEvents.isEmpty()) {
                    Text(
                        "Nothing scheduled for today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        todayEvents.forEach { event ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("•", color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(event.title, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
