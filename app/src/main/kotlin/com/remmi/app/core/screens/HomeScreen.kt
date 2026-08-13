package com.remmi.app.core.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.plugins.PluginManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    pluginManager: PluginManager,
    onWidgetClick: (String) -> Unit
) {
    Log.d("Remmi", "[HomeScreen] - [HomeScreen] executed")
    val metadata by pluginManager.pluginMetadata.collectAsState()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val activeWidgetIds = remember(metadata) {
        metadata.filter { it.enabled && it.showWidget }.map { it.id }.toSet()
    }

    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            // Reload metadata or sync widgets if needed
            // For now, just a delay to show it works
            delay(500)
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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
                pluginManager.getWidgets(activeWidgetIds).forEach { (id, widget) ->
                    Box(modifier = Modifier.clickable { onWidgetClick(id) }) {
                        widget.Content()
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}
