package com.remmi.app.plugins.ingredients.ui.popups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.screens.components.RemmiDatePickerDialog
import com.remmi.app.plugins.ingredients.logic.DefaultIngredientEnrichmentService
import com.remmi.app.core.model.ingredients.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, group: FoodGroup, qty: Double, unit: MeasurementUnit, expiry: LocalDate?, brand: String?, allowedUnits: List<MeasurementUnit>, conversions: List<IngredientConversion>, nutrition: NutritionProfile?, shelfLife: Pair<Int?, Int?>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var foodGroup by remember { mutableStateOf(FoodGroup.VEGETABLES) }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(MeasurementUnit.GRAMS) }
    var brand by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf<LocalDate?>(null) }
    
    // Nutrition State
    var calories by remember { mutableStateOf("") }
    var proteins by remember { mutableStateOf("") }
    var carbohydrates by remember { mutableStateOf("") }
    var sugars by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var sodium by remember { mutableStateOf("") }
    val additionalNutrients = remember { mutableStateListOf<AdditionalNutrient>() }

    // Shelf Life
    var shelfLifeMin by remember { mutableStateOf<Int?>(null) }
    var shelfLifeMax by remember { mutableStateOf<Int?>(null) }

    // Search state
    var isSearching by remember { mutableStateOf(false) }
    var searchMessage by remember { mutableStateOf<String?>(null) }
    val enrichmentService = remember { DefaultIngredientEnrichmentService() }
    val scope = rememberCoroutineScope()

    // Allowed Units Selection
    val allowedUnitsMap = remember { mutableStateMapOf<MeasurementUnit, Boolean>().apply {
        MeasurementUnit.entries.forEach { put(it, it == MeasurementUnit.GRAMS) }
    } }
    
    var gramsPerUnit by remember { mutableStateOf("150") }
    var gramsPerCup by remember { mutableStateOf("240") }

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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name, 
                        onValueChange = { name = it }, 
                        label = { Text("Name*") }, 
                        modifier = Modifier.weight(1f)
                    )
                    
                    Button(
                        onClick = {
                            scope.launch {
                                isSearching = true
                                searchMessage = "Searching..."
                                val result = enrichmentService.searchIngredient(name)
                                name = result.canonicalName
                                foodGroup = result.foodGroup
                                
                                // Populate nutrition
                                val n = result.nutritionPer100g
                                calories = n.calories?.toString() ?: ""
                                proteins = n.proteins?.toString() ?: ""
                                carbohydrates = n.carbohydrates?.toString() ?: ""
                                sugars = n.sugars?.toString() ?: ""
                                fats = n.fats?.toString() ?: ""
                                fiber = n.fiber?.toString() ?: ""
                                sodium = n.sodium?.toString() ?: ""
                                
                                additionalNutrients.clear()
                                additionalNutrients.addAll(n.additionalNutrients)

                                shelfLifeMin = result.estimatedShelfLifeMinDays
                                shelfLifeMax = result.estimatedShelfLifeMaxDays
                                searchMessage = "Found info. Review below."
                                isSearching = false
                            }
                        },
                        enabled = name.isNotBlank() && !isSearching,
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        if (isSearching) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.Search, null)
                    }
                }

                searchMessage?.let { msg ->
                    Text(msg, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                }
                
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
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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

                HorizontalDivider()

                // NUTRITION MANUAL ENTRY
                Text("Nutrition Content (Per 100g)", style = MaterialTheme.typography.titleSmall)
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Calories (kcal)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = proteins,
                            onValueChange = { proteins = it },
                            label = { Text("Protein (g)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = carbohydrates,
                            onValueChange = { carbohydrates = it },
                            label = { Text("Carbs (g)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sugars,
                            onValueChange = { sugars = it },
                            label = { Text("Sugar (g)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = fats,
                            onValueChange = { fats = it },
                            label = { Text("Fat (g)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fiber,
                            onValueChange = { fiber = it },
                            label = { Text("Fiber (g)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = sodium,
                            onValueChange = { sodium = it },
                            label = { Text("Sodium (mg)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                // ADDITIONAL VITAMINS
                Spacer(Modifier.height(8.dp))
                Text("Vitamins & Minerals", style = MaterialTheme.typography.titleSmall)
                
                additionalNutrients.forEachIndexed { index, nutrient ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nutrient.name,
                            onValueChange = { additionalNutrients[index] = nutrient.copy(name = it) },
                            label = { Text("Name") },
                            modifier = Modifier.weight(2f),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                        )
                        OutlinedTextField(
                            value = nutrient.value.toString(),
                            onValueChange = { it.toDoubleOrNull()?.let { v -> additionalNutrients[index] = nutrient.copy(value = v) } },
                            label = { Text("Val") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                        )
                        OutlinedTextField(
                            value = nutrient.unit,
                            onValueChange = { additionalNutrients[index] = nutrient.copy(unit = it) },
                            label = { Text("Unit") },
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                        )
                        IconButton(onClick = { additionalNutrients.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                TextButton(
                    onClick = { additionalNutrients.add(AdditionalNutrient("", 0.0, "mg")) },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add Additional Nutrient")
                }

                HorizontalDivider()
                Text("Measurement & Shelf Life", style = MaterialTheme.typography.titleSmall)
                
                Column {
                    MeasurementUnit.entries.filter { it != MeasurementUnit.KILOGRAMS && it != MeasurementUnit.LITERS }.forEach { u ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = allowedUnitsMap[u] ?: false,
                                onCheckedChange = { allowedUnitsMap[u] = it }
                            )
                            Text(u.name.lowercase())
                        }
                    }
                }

                OutlinedTextField(
                    value = expiryDate?.toString() ?: "None",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Batch Expiry Date") },
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
                onClick = { 
                    val selectedUnits = allowedUnitsMap.filter { it.value }.keys.toList()
                    val conversions = mutableListOf<IngredientConversion>()
                    
                    if (allowedUnitsMap[MeasurementUnit.UNITS] == true) {
                        conversions.add(IngredientConversion(MeasurementUnit.UNITS, MeasurementUnit.GRAMS, gramsPerUnit.toDoubleOrNull() ?: 150.0))
                    }
                    if (allowedUnitsMap[MeasurementUnit.CUPS] == true) {
                        conversions.add(IngredientConversion(MeasurementUnit.CUPS, MeasurementUnit.GRAMS, gramsPerCup.toDoubleOrNull() ?: 240.0))
                    }

                    val profile = NutritionProfile(
                        calories = calories.toDoubleOrNull(),
                        proteins = proteins.toDoubleOrNull(),
                        carbohydrates = carbohydrates.toDoubleOrNull(),
                        sugars = sugars.toDoubleOrNull(),
                        fats = fats.toDoubleOrNull(),
                        fiber = fiber.toDoubleOrNull(),
                        sodium = sodium.toDoubleOrNull(),
                        additionalNutrients = additionalNutrients.toList()
                    )
                    
                    onConfirm(name, foodGroup, quantity.toDoubleOrNull() ?: 0.0, unit, expiryDate, brand.takeIf { it.isNotBlank() }, selectedUnits, conversions, profile, shelfLifeMin to shelfLifeMax) 
                },
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
