package com.remmi.app.ui.screens.launcher

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.android.launcher.LauncherManagerService
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.screens.launcher.data.LauncherPreferencesRepository
import com.remmi.app.ui.screens.launcher.drawer.AppDrawerSheet
import com.remmi.app.ui.screens.launcher.modules.*

@Composable
fun LauncherDashboardScreen(
    controller: RemmiController,
    onNavigateToPlugin: (String) -> Unit,
    onNavigateToLauncherSettings: () -> Unit
) {
    val context = LocalContext.current
    val prefsRepo = remember { LauncherPreferencesRepository(context) }
    val launcherManager = remember { LauncherManagerService(context) }

    var showAppDrawer by remember { mutableStateOf(false) }
    val isDefaultLauncher = remember { launcherManager.isDefaultLauncher() }

    // Instantiate modules
    val allModules = remember {
        listOf<LauncherModule>(
            TodayModule(),
            CalendarLauncherModule(),
            TasksLauncherModule(),
            ShoppingLauncherModule(),
            QuickActionsLauncherModule(),
            ContactsLauncherModule(),
            AppWidgetHostModule()
        )
    }

    val enabledModules = remember(prefsRepo) {
        val configs = prefsRepo.getModuleConfigs().filter { it.enabled }.associateBy { it.type }
        allModules.filter { module ->
            configs[module.type]?.enabled ?: true
        }.sortedBy { module ->
            configs[module.type]?.priority ?: module.priority
        }
    }

    var totalDragY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (totalDragY < -150f) {
                            // Swipe UP -> App Drawer
                            showAppDrawer = true
                        } else if (totalDragY > 150f) {
                            // Swipe DOWN -> Notifications
                            if (prefsRepo.isSwipeDownNotifications) {
                                launcherManager.openNotificationShade()
                            } else {
                                showAppDrawer = true
                            }
                        }
                        totalDragY = 0f
                    },
                    onDragCancel = { totalDragY = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        totalDragY += dragAmount
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showAppDrawer = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Apps",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "REMMI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (!isDefaultLauncher) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = "App Mode",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(onClick = onNavigateToLauncherSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Launcher Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Dashboard Modules List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(enabledModules, key = { it.id }) { module ->
                    module.Content(
                        controller = controller,
                        onNavigateToPlugin = onNavigateToPlugin
                    )
                }
            }
        }

        // Floating App Drawer Trigger Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, _ -> }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showAppDrawer = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "App Drawer",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Apps & Search",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        // App Drawer Sheet
        if (showAppDrawer) {
            AppDrawerSheet(
                onDismissRequest = { showAppDrawer = false }
            )
        }
    }
}
