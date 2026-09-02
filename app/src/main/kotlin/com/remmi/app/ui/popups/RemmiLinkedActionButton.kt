package com.remmi.app.ui.popups

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * REMMI LINKED ACTION BUTTON
 * Standardized button for linking items (Alarm, Task, etc.) across plugins.
 * Fixes the "target box" issue by disabling the default Material 3 minimum interactive size.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemmiLinkedActionButton(
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Strictly disable the mandatory touch target expansion to avoid visual artifacts/shadows
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Surface(
            onClick = onClick,
            modifier = modifier.size(56.dp),
            shape = CircleShape,
            color = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
