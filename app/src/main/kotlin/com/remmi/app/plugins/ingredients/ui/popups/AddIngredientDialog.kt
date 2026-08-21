package com.remmi.app.plugins.ingredients.ui.popups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.screens.components.RemmiDatePickerDialog
import com.remmi.app.plugins.ingredients.models.*
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, group: FoodGroup, qty: Double, unit: MeasurementUnit, expiry: LocalDate?, brand: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var foodGroup by remember { mutableStateOf(FoodGroup.VEGETABLES) }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(MeasurementUnit.GRAMS) }
    var brand by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf<LocalDate?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val today = remember { 
        Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            .toLocalDateTime(TimeZone.currentSystemDefault()).date 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Ingredient") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name*") }, modifier = Modifier.fillMaxWidth())
                
                // Food Group Selector
                var groupExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = foodGroup.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Food Group") },
                        trailingIcon = { IconButton(onClick = { groupExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                        FoodGroup.entries.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = { foodGroup = group; groupExpanded = false }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity, 
                        onValueChange = { if (it.toDoubleOrNull() != null || it.isEmpty()) quantity = it }, 
                        label = { Text("Initial Qty") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    var unitExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = unit.name.lowercase(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { IconButton(onClick = { unitExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                            MeasurementUnit.entries.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u.name.lowercase()) },
                                    onClick = { unit = u; unitExpanded = false }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand (Optional)") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = expiryDate?.toString() ?: "None",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Expiry Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, foodGroup, quantity.toDoubleOrNull() ?: 0.0, unit, expiryDate, brand.takeIf { it.isNotBlank() }) },
                enabled = name.isNotBlank() && quantity.toDoubleOrNull() != null
            ) {
                Text("Add")
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
