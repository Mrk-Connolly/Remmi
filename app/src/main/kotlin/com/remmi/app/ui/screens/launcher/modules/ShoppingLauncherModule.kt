package com.remmi.app.ui.screens.launcher.modules

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.commands.CreateShoppingItemCommand
import com.remmi.app.core.eventBus.commands.ToggleShoppingItemCommand
import com.remmi.app.plugins.shopping_list.ShoppingListPlugin
import kotlinx.coroutines.launch

class ShoppingLauncherModule : LauncherModule {
    override val id: String = "shopping_module"
    override val title: String = "Shopping List"
    override val type: LauncherModuleType = LauncherModuleType.SHOPPING
    override val priority: Int = 3

    @Composable
    override fun Content(
        controller: RemmiController,
        onNavigateToPlugin: (String) -> Unit
    ) {
        val coroutineScope = rememberCoroutineScope()
        var newItemText by remember { mutableStateOf("") }

        val shoppingPlugin = controller.pluginManager.plugins["shopping_list"] as? ShoppingListPlugin
        val items by shoppingPlugin?.repository?.asFlow()?.collectAsState(initial = emptyList())
            ?: remember { mutableStateOf(emptyList()) }

        val activeItems = items.filter { !it.completed }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPlugin("shopping_list") },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Shopping",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SHOPPING — ${activeItems.size} ITEMS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Shopping List",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Display items summary
                if (activeItems.isEmpty()) {
                    Text(
                        text = "Shopping list is empty",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                } else {
                    activeItems.take(4).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        controller.eventBus.publishCommand(
                                            ToggleShoppingItemCommand(itemId = item.id, source = "launcher")
                                        )
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.completed) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = "Toggle Item",
                                    tint = if (item.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToPlugin("shopping_list") }
                            )
                        }
                    }
                    if (activeItems.size > 4) {
                        Text(
                            text = "+ ${activeItems.size - 4} more items",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding(top = 4.dp, start = 36.dp)
                                .clickable { onNavigateToPlugin("shopping_list") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick add item input
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    placeholder = { Text("Add shopping item...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    trailingIcon = {
                        if (newItemText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val textToAdd = newItemText.trim()
                                    newItemText = ""
                                    coroutineScope.launch {
                                        controller.eventBus.publishCommand(
                                            CreateShoppingItemCommand(name = textToAdd, source = "launcher")
                                        )
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Item")
                            }
                        }
                    }
                )
            }
        }
    }
}
