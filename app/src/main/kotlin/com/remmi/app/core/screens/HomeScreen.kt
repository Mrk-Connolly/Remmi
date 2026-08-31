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
import com.remmi.app.core.plugin.PluginManager
import kotlinx.coroutines.launch

/**
 * HOME SCREEN
 * Main landing page of the application, displaying active widgets
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    pluginManager: PluginManager,
    onWidgetClick: (String) -> Unit
) {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    Log.d("Remmi", "[HomeScreen] - [HomeScreen] executed")
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    // We observe metadata change to trigger recomposition when settings change
    val metadata by pluginManager.pluginMetadata.collectAsState()

    // Calculate visible widgets when metadata or plugins change
    val visiblePlugins = remember(metadata, pluginManager.plugins) {
        pluginManager.plugins.values.filter { it.widget.isEnabled() }
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 On Refresh
     * Logic for pull-to-refresh action
     * */
    val onRefresh: () -> Unit = remember {
        {
            scope.launch {
                isRefreshing = true
                pluginManager.refreshAllPlugins()
                isRefreshing = false
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Good morning,",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "Mark", // In a real app, this would be the user's name
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Render all enabled widgets
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                visiblePlugins.forEach { plugin ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWidgetClick(plugin.metadata.id) }
                    ) {
                        plugin.widget.Content()
                    }
                }
            }
        }
    }
}
