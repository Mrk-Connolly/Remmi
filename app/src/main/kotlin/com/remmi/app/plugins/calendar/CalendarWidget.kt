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
                Spacer(Modifier.height(8.dp))

                if (todayEvents.isEmpty()) {
                    Text("No events today", style = MaterialTheme.typography.bodySmall)
                } else {
                    todayEvents.forEach { event ->
                        Text("• ${event.title}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
