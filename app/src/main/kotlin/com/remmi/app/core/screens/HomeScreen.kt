package com.remmi.app.core.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.widgets.WidgetManager

@Composable
fun HomeScreen(widgetManager: WidgetManager) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Widgets at the top
        widgetManager.getWidgets().forEach { it.Content() }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Hello, I'm Remmi",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Your personal assistant",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}