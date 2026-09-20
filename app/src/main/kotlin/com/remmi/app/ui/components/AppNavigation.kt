package com.remmi.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.remmi.app.core.controller.GlobalUIState
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.models.Category
import com.remmi.app.core.models.Categories
import com.remmi.app.ui.DesignTokens
import com.remmi.app.ui.navigation.RemmiScreenRegistry
import com.remmi.app.ui.screens.homescreen.CategoryOverview
import com.remmi.app.ui.screens.homescreen.HomeScreen
import com.remmi.app.ui.screens.settings.AutomatizationSettingsScreen
import com.remmi.app.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.launch

/**
 * NEW navigation orchestrator for the Remmi application.
 * Implements the Category-based interaction model with horizontal swiping and right-side launcher.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    runtime: RemmiController
) {
    val navController = rememberNavController()
    
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = RemmiDestination.HOME.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(RemmiDestination.HOME.route) {
                HomeScreen(
                    onCategoryClick = { category ->
                        navController.navigate(RemmiDestination.categoryRoute(category.id))
                    },
                    onProfileClick = { /* Profile Action */ },
                    onSettingsClick = { navController.navigate(RemmiDestination.SETTINGS.route) }
                )
            }

            composable(RemmiDestination.CATEGORY.route) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                val category = Categories.find { it.id == categoryId }
                
                if (category != null) {
                    CategoryOverview(
                        category = category,
                        runtime = runtime,
                        onWidgetClick = { pluginId ->
                            navController.navigate(RemmiDestination.pluginRoute(pluginId))
                        },
                        onProfileClick = { /* Profile Action */ },
                        onSettingsClick = { navController.navigate(RemmiDestination.SETTINGS.route) }
                    )
                }
            }

            composable(RemmiDestination.SETTINGS.route) {
                SettingsScreen(
                    runtime = runtime,
                    navController = navController,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(RemmiDestination.AUTOMATIZATION_ROUTE) {
                AutomatizationSettingsScreen(
                    controller = runtime,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("plugin/{pluginId}") { backStackEntry ->
                val pluginId = backStackEntry.arguments?.getString("pluginId") ?: ""
                val screenContent = RemmiScreenRegistry.getScreen(pluginId)
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    if (screenContent != null) {
                        screenContent(runtime)
                    } else {
                        val plugin = runtime.pluginManager.plugins[pluginId]
                        if (plugin != null) {
                            plugin.screen.Content(controller = runtime)
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Plugin not found: $pluginId")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopNavigationOverlay(
    titles: List<String>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // SETTINGS (LEFT)
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
        }

        // TITLE & DOT INDICATORS
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val currentTitle = titles.getOrNull(pagerState.currentPage) ?: ""
            Text(
                text = currentTitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                letterSpacing = 2.sp
            )
            
            if (titles.size > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(titles.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val size by animateDpAsState(targetValue = if (isSelected) 10.dp else 6.dp, label = "dot_size")
                        val alpha by animateFloatAsState(targetValue = if (isSelected) 1f else 0.3f, label = "dot_alpha")

                        Box(
                            modifier = Modifier
                                .size(size)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                        )
                    }
                }
            }
        }

        // PROFILE (RIGHT)
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}



sealed class RemmiDestination(val route: String) {
    data object HOME : RemmiDestination("main")
    data object CATEGORY : RemmiDestination("category/{categoryId}")
    data object SETTINGS : RemmiDestination("settings")
    companion object {
        const val AUTOMATIZATION_ROUTE = "settings/automatization"
        fun pluginRoute(id: String): String = "plugin/$id"
        fun categoryRoute(id: String): String = "category/$id"
    }
}
