package com.remmi.app.plugins.alarm

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.widgets.RemmiWidget
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Dashboard widget for the Alarms plugin.
 */
class AlarmWidget(private val actions: AlarmActions) : RemmiWidget {

    @Composable
    override fun Content() {
        var alarms by remember { mutableStateOf(emptyList<AlarmItem>()) }

        LaunchedEffect(Unit) {
            alarms = actions.getAllAlarms().take(3) // Show top 3
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⏰ Upcoming Alarms",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                if (alarms.isEmpty()) {
                    Text("No alarms set", style = MaterialTheme.typography.bodySmall)
                } else {
                    val timeZone = TimeZone.currentSystemDefault()
                    alarms.forEach { alarm ->
                        val localDateTime = alarm.time.toLocalDateTime(timeZone)
                        val timeStr = "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
                        
                        Text(
                            text = "• $timeStr - ${alarm.title}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
