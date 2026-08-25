package com.remmi.app.plugins.ingredients.ui.popups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.ui.components.NutritionRadarGraph
import com.remmi.app.core.model.ingredients.IngredientUiModel
import com.remmi.app.core.model.ingredients.formatQuantity
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        InfoRow("Food Group", item.metadata.foodGroup.name.replace("_", " "))
                        if (!item.metadata.brand.isNullOrBlank()) InfoRow("Brand", item.metadata.brand)
                        InfoRow("Location", item.stock.storageLocation.name)
                    }
                    
                    // Expiry/Shelf Life Section
                    item.metadata.estimatedShelfLifeMinDays?.let { min ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("EST. SHELF LIFE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                val max = item.metadata.estimatedShelfLifeMaxDays
                                val lifeText = if (max != null) "$min-$max days" else "$min days"
                                Text(lifeText, fontSize = 12.sp)
                            }
                        }
                    }
                }
                
                if (item.metadata.description.isNotBlank()) InfoRow("Description", item.metadata.description)

                HorizontalDivider()

                // NUTRITION SECTION
                Text("Nutrition (Per 100g)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                item.metadata.baseNutrition?.let { nutrition ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${nutrition.calories?.toInt() ?: "--"} kcal",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        NutritionRadarGraph(nutrition = nutrition)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Detailed Values
                        val gridModifier = Modifier.fillMaxWidth()
                        Row(gridModifier) {
                            NutritionItem("Proteins", "${nutrition.proteins ?: "--"} g", Modifier.weight(1f))
                            NutritionItem("Carbs", "${nutrition.carbohydrates ?: "--"} g", Modifier.weight(1f))
                        }
                        Row(gridModifier) {
                            NutritionItem("Sugars", "${nutrition.sugars ?: "--"} g", Modifier.weight(1f))
                            NutritionItem("Fiber", "${nutrition.fiber ?: "--"} g", Modifier.weight(1f))
                        }
                        Row(gridModifier) {
                            NutritionItem("Fats", "${nutrition.fats ?: "--"} g", Modifier.weight(1f))
                            NutritionItem("Sodium", "${nutrition.sodium?.toInt() ?: "--"} mg", Modifier.weight(1f))
                        }
                    }
                    
                    if (nutrition.additionalNutrients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Vitamins & Minerals", style = MaterialTheme.typography.titleSmall)
                        nutrition.additionalNutrients.forEach { nut ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(nut.name, style = MaterialTheme.typography.bodySmall)
                                Text("${nut.value} ${nut.unit}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } ?: Text("No nutrition data available.", style = MaterialTheme.typography.bodySmall)

                HorizontalDivider()

                // Batches
                Text("Stock Batches", style = MaterialTheme.typography.titleSmall)
                if (item.batches.isEmpty()) {
                    Text("No active batches.", style = MaterialTheme.typography.bodySmall)
                } else {
                    item.batches.forEach { batch ->
                        val (formattedQty, formattedUnit) = formatQuantity(batch.quantity, item.stock.primaryUnit)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("$formattedQty $formattedUnit", style = MaterialTheme.typography.bodyMedium)
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
fun NutritionItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
