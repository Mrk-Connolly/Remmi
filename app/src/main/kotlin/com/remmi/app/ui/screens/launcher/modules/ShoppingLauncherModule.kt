package com.remmi.app.ui.screens.launcher.modules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController

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
        var newItemText by remember { mutableStateOf("") }
        val sampleItems = remember { mutableStateListOf("Milk", "Bread", "Coffee", "Eggs", "Apples") }

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
                            text = "SHOPPING — ${sampleItems.size} ITEMS",
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

                Spacer(modifier = Modifier.height(12.dp))

                // Display items summary
                if (sampleItems.isEmpty()) {
                    Text(
                        text = "Shopping list is empty",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                } else {
                    sampleItems.take(3).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onNavigateToPlugin("shopping_list") },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "☐  $item",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (sampleItems.size > 3) {
                        Text(
                            text = "+ ${sampleItems.size - 3} more items",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { onNavigateToPlugin("shopping_list") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick add item input
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    placeholder = { Text("Add shopping item...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (newItemText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    sampleItems.add(newItemText.trim())
                                    newItemText = ""
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
