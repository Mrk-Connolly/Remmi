package com.remmi.app.core.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.widgets.WidgetManager

@Composable
fun HomeScreen(
    pluginManager: PluginManager,
    widgetManager: WidgetManager,
    onWidgetClick: (String) -> Unit
) {
    val metadata by pluginManager.pluginMetadata.collectAsState()
    val activeWidgetIds = remember(metadata) {
        metadata.filter { it.enabled && it.showWidget }.map { it.id }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Hello, I'm Remmi",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Your personal assistant",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Widgets below the greeting
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            widgetManager.getWidgets(activeWidgetIds).forEach { (id, widget) ->
                Box(modifier = Modifier.clickable { onWidgetClick(id) }) {
                    widget.Content()
                }
            }
        }
    }
}
