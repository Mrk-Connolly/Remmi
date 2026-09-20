package com.remmi.app.ui.screens.homescreen

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.models.Category
import com.remmi.app.core.plugin.PluginManager
import com.remmi.app.ui.components.RemmiCard
import com.remmi.app.ui.components.TopNavigationOverlay
import com.remmi.app.ui.navigation.RemmiScreenRegistry
import kotlinx.coroutines.launch

import com.remmi.app.ui.components.LocalRemmiScaffoldConfig
import com.remmi.app.ui.components.RemmiScaffoldConfig

/**
 * CATEGORY OVERVIEW
 * A pager-based screen for a specific Category, starting with a widget overview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryOverview(
    category: Category,
    runtime: RemmiController,
    onWidgetClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val pluginManager = runtime.pluginManager
    Log.d("Remmi", "[CategoryOverview] - Rendering overview for: ${category.name}")
    
    val metadata by pluginManager.pluginMetadata.collectAsState()

    // Filter plugins belonging to this category
    val categoryPlugins = remember(metadata, pluginManager.plugins, category) {
        category.plugins.mapNotNull { pluginManager.plugins[it] }
    }
    
    val visiblePlugins = remember(categoryPlugins) {
        categoryPlugins.filter { it.widget.isEnabled() }
    }

    // Pager titles: "Overview" + plugin names
    val pageTitles = remember(categoryPlugins) {
        listOf("Overview") + categoryPlugins.map { it.metadata.name }
    }
    
    val pagerState = rememberPagerState(initialPage = 0) { pageTitles.size }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Plugin Content (Background) - Fills the whole screen
        CompositionLocalProvider(
            LocalRemmiScaffoldConfig provides RemmiScaffoldConfig(
                useTopOverlay = true,
                topContentPadding = 100.dp // Offset content to start below the top panel buttons
            )
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { pageIndex ->
                if (pageIndex == 0) {
                    CategorySummary(
                        category = category,
                        visiblePlugins = visiblePlugins,
                        pluginManager = pluginManager,
                        onWidgetClick = onWidgetClick
                    )
                } else {
                    val plugin = categoryPlugins[pageIndex - 1]
                    val screenContent = RemmiScreenRegistry.getScreen(plugin.metadata.id)
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (screenContent != null) {
                            screenContent(runtime)
                        } else {
                            plugin.screen.Content(controller = runtime)
                        }
                    }
                }
            }
        }

        // 2. TOP NAVIGATION OVERLAY (Foreground)
        TopNavigationOverlay(
            titles = pageTitles,
            pagerState = pagerState,
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySummary(
    category: Category,
    visiblePlugins: List<com.remmi.app.core.plugin.RemmiPlugin>,
    pluginManager: PluginManager,
    onWidgetClick: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val config = LocalRemmiScaffoldConfig.current

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
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = config.topContentPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Dashboard Widgets Section
            if (visiblePlugins.isEmpty()) {
                RemmiCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No active plugins",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.animateContentSize()
                ) {
                    visiblePlugins.forEach { plugin ->
                        RemmiCard(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onWidgetClick(plugin.metadata.id) },
                            elevation = 1.dp
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                plugin.widget.Content()
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(120.dp)) // Extra space at bottom
        }
    }
}
