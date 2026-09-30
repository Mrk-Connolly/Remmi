package com.remmi.app.ui.screens.launcher

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showAppDrawer = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Apps",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "REMMI DASHBOARD",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
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

            // Dashboard Modules List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
        FloatingActionButton(
            onClick = { showAppDrawer = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Apps, contentDescription = "App Drawer")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Apps",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
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
