package com.remmi.app.plugins.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.widgets.RemmiWidget

/**
 * Dashboard widget implementation for the Calendar plugin.
 *
 * Displays a quick summary of upcoming events on the main home screen.
 */
class CalendarWidget : RemmiWidget {

    /**
     * Renders the widget's content as a themed Card.
     */
    @Composable
    override fun Content() {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📅 Calendar",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Upcoming: No meetings",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
