package com.remmi.app.plugins.ingredients.popups

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.screens.components.RemmiDatePickerDialog
import com.remmi.app.plugins.ingredients.models.IngredientUiModel
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustmentDialog(
    item: IngredientUiModel,
    onDismiss: () -> Unit,
    onConfirm: (delta: Double, expiry: LocalDate?) -> Unit
) {
    var deltaStr by remember { mutableStateOf("") }
    var isAddition by remember { mutableStateOf(true) }
    var expiryDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val today = remember { 
        Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            .toLocalDateTime(TimeZone.currentSystemDefault()).date 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Stock: ${item.metadata.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = !isAddition,
                        onClick = { isAddition = false },
                        label = { Text("-") }
                    )
                    Spacer(Modifier.width(16.dp))
                    FilterChip(
                        selected = isAddition,
                        onClick = { isAddition = true },
                        label = { Text("+") }
                    )
                }

                OutlinedTextField(
                    value = deltaStr,
                    onValueChange = { if (it.toDoubleOrNull() != null || it.isEmpty()) deltaStr = it },
                    label = { Text("Amount (${item.stock.primaryUnit.name.lowercase()})") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isAddition) {
                    OutlinedTextField(
                        value = expiryDate?.toString() ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Batch Expiry") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarMonth, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val delta = deltaStr.toDoubleOrNull() ?: 0.0
                    onConfirm(if (isAddition) delta else -delta, if (isAddition) expiryDate else null)
                },
                enabled = deltaStr.toDoubleOrNull() != null && deltaStr.toDouble() > 0
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        RemmiDatePickerDialog(
            initialDate = expiryDate ?: today,
            onDismiss = { showDatePicker = false },
            onDateSelected = { expiryDate = it; showDatePicker = false }
        )
    }
}
