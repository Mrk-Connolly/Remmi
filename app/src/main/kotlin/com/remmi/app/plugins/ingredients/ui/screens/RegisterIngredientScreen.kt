package com.remmi.app.plugins.ingredients.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
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
import com.remmi.app.ui.components.*
import com.remmi.app.plugins.ingredients.IngredientActions
import com.remmi.app.plugins.ingredients.models.*
import com.remmi.app.plugins.ingredients.logic.DefaultIngredientEnrichmentService
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@Composable
fun RegisterIngredientScreen(
    actions: IngredientActions,
    initialName: String = "",
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val enrichmentService = remember { DefaultIngredientEnrichmentService() }

    var name by remember { mutableStateOf(initialName) }
    var foodGroup by remember { mutableStateOf(FoodGroup.VEGETABLES) }
    var shelfLifeMin by remember { mutableStateOf("") }
    var shelfLifeMax by remember { mutableStateOf("") }

    // Nutrition State
    var calories by remember { mutableStateOf("") }
    var proteins by remember { mutableStateOf("") }
    var carbohydrates by remember { mutableStateOf("") }
    var sugars by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var sodium by remember { mutableStateOf("") }
    val additionalNutrients = remember { mutableStateListOf<AdditionalNutrient>() }

    var isSearching by remember { mutableStateOf(false) }

    RemmiAddScreen(
        title = "Register Ingredient",
        onBack = onBack,
        onSave = {
            scope.launch {
                val nutrition = NutritionProfile(
                    calories = calories.toDoubleOrNull(),
                    proteins = proteins.toDoubleOrNull(),
                    carbohydrates = carbohydrates.toDoubleOrNull(),
                    sugars = sugars.toDoubleOrNull(),
                    fats = fats.toDoubleOrNull(),
                    fiber = fiber.toDoubleOrNull(),
                    sodium = sodium.toDoubleOrNull(),
                    additionalNutrients = additionalNutrients.toList()
                )
                
                // We just register the metadata here. In a real app, this might create a row in ingredient_metadata.
                // For this task, we assume addIngredient handles everything or we just want to save metadata.
                // Since actions.addIngredient creates metadata, we can either use that or add a registerMetadata action.
                // For now, let's assume the user just wants to register it for later use.
                
                actions.addIngredient(
                    name = name,
                    foodGroup = foodGroup,
                    initialQuantity = 0.0,
                    unit = MeasurementUnit.UNITS,
                    baseNutrition = nutrition,
                    shelfLife = (shelfLifeMin.toIntOrNull() ?: 0) to (shelfLifeMax.toIntOrNull() ?: 0)
                )
                onBack()
            }
        },
        saveEnabled = name.isNotEmpty()
    ) {
        RemmiSectionHeader("Basic Details")
        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ingredient Name") },
                modifier = Modifier.weight(1f),
                shape = CircleShape
            )
            
            IconButton(
                onClick = {
                    scope.launch {
                        isSearching = true
                        val result = enrichmentService.searchIngredient(name)
                        name = result.canonicalName
                        foodGroup = result.foodGroup
                        
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

                        shelfLifeMin = result.estimatedShelfLifeMinDays?.toString() ?: ""
                        shelfLifeMax = result.estimatedShelfLifeMaxDays?.toString() ?: ""
                        isSearching = false
                    }
                },
                enabled = name.isNotBlank() && !isSearching,
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                if (isSearching) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Search, contentDescription = "Search & Preload")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = shelfLifeMin,
                onValueChange = { shelfLifeMin = it },
                label = { Text("Min Shelf Life (days)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = CircleShape
            )
            OutlinedTextField(
                value = shelfLifeMax,
                onValueChange = { shelfLifeMax = it },
                label = { Text("Max Shelf Life (days)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = CircleShape
            )
        }

        RemmiSectionHeader("Nutrition (Per 100g/ml)")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                label = { Text("Calories (kcal)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = CircleShape
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = proteins,
                    onValueChange = { proteins = it },
                    label = { Text("Protein (g)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = CircleShape
                )
                OutlinedTextField(
                    value = carbohydrates,
                    onValueChange = { carbohydrates = it },
                    label = { Text("Carbs (g)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = CircleShape
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = sugars,
                    onValueChange = { sugars = it },
                    label = { Text("Sugars (g)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = CircleShape
                )
                OutlinedTextField(
                    value = fats,
                    onValueChange = { fats = it },
                    label = { Text("Fat (g)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = CircleShape
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fiber,
                    onValueChange = { fiber = it },
                    label = { Text("Fiber (g)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = CircleShape
                )
                OutlinedTextField(
                    value = sodium,
                    onValueChange = { sodium = it },
                    label = { Text("Sodium (mg)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = CircleShape
                )
            }
        }

        RemmiSectionHeader("Vitamins & Minerals")
        additionalNutrients.forEachIndexed { index, nutrient ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nutrient.name,
                    onValueChange = { additionalNutrients[index] = nutrient.copy(name = it) },
                    label = { Text("Name") },
                    modifier = Modifier.weight(2f),
                    shape = CircleShape
                )
                OutlinedTextField(
                    value = nutrient.value.toString(),
                    onValueChange = { it.toDoubleOrNull()?.let { v -> additionalNutrients[index] = nutrient.copy(value = v) } },
                    label = { Text("Value") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = CircleShape
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
            Spacer(Modifier.width(8.dp))
            Text("Add Nutrient")
        }
        
        Spacer(Modifier.height(32.dp))
    }
}
