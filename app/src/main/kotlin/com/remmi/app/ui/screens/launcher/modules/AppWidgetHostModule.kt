package com.remmi.app.ui.screens.launcher.modules

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.screens.launcher.data.LauncherPreferencesRepository

class AppWidgetHostModule : LauncherModule {
    override val id: String = "app_widgets_module"
    override val title: String = "Android Widgets"
    override val type: LauncherModuleType = LauncherModuleType.APP_WIDGETS
    override val priority: Int = 6

    companion object {
        const val APP_WIDGET_HOST_ID = 2026
    }

    @Composable
    override fun Content(
        controller: RemmiController,
        onNavigateToPlugin: (String) -> Unit
    ) {
        val context = LocalContext.current
        val prefsRepo = remember { LauncherPreferencesRepository(context) }
        val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
        val appWidgetHost = remember { AppWidgetHost(context, APP_WIDGET_HOST_ID) }

        var widgetIds by remember { mutableStateOf(prefsRepo.getWidgetIds()) }

        DisposableEffect(Unit) {
            appWidgetHost.startListening()
            onDispose {
                try {
                    appWidgetHost.stopListening()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Widgets",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ANDROID APP WIDGETS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (widgetIds.isEmpty()) {
                    Text(
                        text = "No Android App Widgets placed on dashboard yet. Enable widget host and allocate widgets in launcher settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    widgetIds.forEach { widgetId ->
                        val widgetInfo = remember(widgetId) {
                            appWidgetManager.getAppWidgetInfo(widgetId)
                        }

                        if (widgetInfo != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                AndroidView(
                                    factory = { ctx ->
                                        appWidgetHost.createView(ctx, widgetId, widgetInfo).apply {
                                            setAppWidget(widgetId, widgetInfo)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 100.dp, max = 300.dp)
                                )
                                IconButton(
                                    onClick = {
                                        appWidgetHost.deleteAppWidgetId(widgetId)
                                        prefsRepo.removeWidgetId(widgetId)
                                        widgetIds = prefsRepo.getWidgetIds()
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove Widget",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
