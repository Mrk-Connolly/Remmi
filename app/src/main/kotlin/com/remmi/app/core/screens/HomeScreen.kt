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
    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            // In the future, this could trigger a global sync
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

            // Render all enabled widgets
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                visiblePlugins.forEach { plugin ->
                    Box(modifier = Modifier.clickable { onWidgetClick(plugin.metadata.id) }) {
                        plugin.widget.Content()
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}
