package com.remmi.app.plugins.ingredients.ui.popups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.plugins.ingredients.models.IngredientUiModel
import com.remmi.app.plugins.ingredients.ui.screens.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDetailDialog(
    item: IngredientUiModel,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.metadata.name) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Section
                InfoRow("Food Group", item.metadata.foodGroup.name.replace("_", " "))
                if (!item.metadata.brand.isNullOrBlank()) InfoRow("Brand", item.metadata.brand)
                if (item.metadata.description.isNotBlank()) InfoRow("Description", item.metadata.description)
                InfoRow("Location", item.stock.storageLocation.name)

                HorizontalDivider()

                // Nutrition Snippet (Standardized per 100g/ml if available)
                Text("Nutrition", style = MaterialTheme.typography.titleSmall)
                item.metadata.baseNutrition?.let { nutrition ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NutritionMini("Calories", "${nutrition.calories ?: 0}")
                        NutritionMini("Protein", "${nutrition.protein ?: 0}g")
                        NutritionMini("Carbs", "${nutrition.carbohydrates ?: 0}g")
                        NutritionMini("Fat", "${nutrition.fat ?: 0}g")
                    }
                } ?: Text("No nutrition data available.", style = MaterialTheme.typography.bodySmall)

                HorizontalDivider()

                // Batches
                Text("Stock Batches", style = MaterialTheme.typography.titleSmall)
                if (item.batches.isEmpty()) {
                    Text("No active batches.", style = MaterialTheme.typography.bodySmall)
                } else {
                    item.batches.forEach { batch ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${batch.quantity} ${item.stock.primaryUnit.name.lowercase()}", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = batch.expiryDate?.let { "Exp: ${formatDate(it)}" } ?: "No Expiry",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {
            TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text("Delete Item")
            }
        }
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row {
        Text("$label: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun NutritionMini(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}
