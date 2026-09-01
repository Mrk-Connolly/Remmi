package com.remmi.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.remmi.app.ui.DesignTokens

@Composable
fun RemmiCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            modifier = modifier,
            onClick = onClick,
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            content = content
        )
    }
}

@Composable
fun RemmiSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = DesignTokens.SpacingSmall)
    )
}

/**
 * REMMI TITLE DESCRIPTION GROUP
 * Combined input fields for common item properties (title and description)
 */
@Composable
fun RemmiTitleDescriptionGroup(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * REMMI PRIORITY SWITCH
 * Standardized toggle for high-priority items
 */
@Composable
fun RemmiPrioritySwitch(
    isPriority: Boolean,
    onPriorityChange: (Boolean) -> Unit,
    label: String = "Priority",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Switch(checked = isPriority, onCheckedChange = onPriorityChange)
    }
}

/**
 * Returns an ImageVector for a given icon name.
 * Fallback to help if not found.
 */
fun getIconForName(name: String?): ImageVector {
    return when (name?.lowercase()) {
        "calendar" -> Icons.Default.CalendarMonth
        "tasks" -> Icons.Default.Task
        "settings" -> Icons.Default.Settings
        "home" -> Icons.Default.Home
        "alarm" -> Icons.Default.Alarm
        "weather" -> Icons.Default.Cloud
        "map", "location" -> Icons.Default.Place
        "gift" -> Icons.Default.CardGiftcard
        "contact", "people" -> Icons.Default.Person
        "restaurant", "food" -> Icons.Default.Restaurant
        "receipt" -> Icons.Default.Receipt
        "lock" -> Icons.Default.Lock
        "notifications" -> Icons.Default.Notifications
        else -> Icons.Default.Help
    }
}
