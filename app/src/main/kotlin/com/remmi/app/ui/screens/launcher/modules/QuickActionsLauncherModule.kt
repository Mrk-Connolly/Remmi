package com.remmi.app.ui.screens.launcher.modules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController

private data class QuickActionItem(
    val pluginId: String,
    val label: String,
    val icon: ImageVector
)

class QuickActionsLauncherModule : LauncherModule {
    override val id: String = "quick_actions_module"
    override val title: String = "Quick Access"
    override val type: LauncherModuleType = LauncherModuleType.QUICK_ACTIONS
    override val priority: Int = 4

    @Composable
    override fun Content(
        controller: RemmiController,
        onNavigateToPlugin: (String) -> Unit
    ) {
        val actions = listOf(
            QuickActionItem("calendar", "Calendar", Icons.Default.CalendarToday),
            QuickActionItem("shopping_list", "Shopping", Icons.Default.ShoppingCart),
            QuickActionItem("contacts", "Contacts", Icons.Default.Contacts),
            QuickActionItem("tasks", "Tasks", Icons.Default.CheckCircle),
            QuickActionItem("weather", "Weather", Icons.Default.WbSunny),
            QuickActionItem("maps", "Maps", Icons.Default.Map)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Text(
                text = "QUICK ACCESS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(actions) { action ->
                    Surface(
                        modifier = Modifier.clickable { onNavigateToPlugin(action.pluginId) },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.label,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = action.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
