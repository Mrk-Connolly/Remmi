package com.remmi.app.plugins.recipebook.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.components.RemmiAddScreen
import com.remmi.app.plugins.ingredients.IngredientActions
import com.remmi.app.plugins.ingredients.models.*
import com.remmi.app.plugins.recipebook.RecipeActions
import com.remmi.app.plugins.recipebook.models.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRecipeScreen(
    actions: RecipeActions,
    controller: RemmiController,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val ingredientPlugin = controller.pluginManager.plugins["ingredient_stock"]
    val ingredientActions = ingredientPlugin?.actions as? IngredientActions
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var servings by remember { mutableIntStateOf(1) }
    
    var prepTime by remember { mutableIntStateOf(0) }
    var cookingTime by remember { mutableIntStateOf(0) }
    var ovenTime by remember { mutableIntStateOf(0) }
    var restingTime by remember { mutableIntStateOf(0) }
    
    val totalTime = prepTime + cookingTime + ovenTime + restingTime
    
    var selectedIngredients by remember { mutableStateOf(setOf<IngredientUiModel>()) }
    var steps by remember { mutableStateOf(listOf(RecipeStep(1, ""))) }
    
    var showIngredientPicker by remember { mutableStateOf(false) }
    var showAddIngredientDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val onSave = {
        val id = UUID.randomUUID().toString()
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        var savedImagePath: String? = null
        imageUri?.let { uri ->
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    savedImagePath = controller.androidManager.fileService.saveImage(
                        bytes,
                        "DCIM/Remmi/RecipeBook",
                        "recipe_$id.jpg"
                    )
                }
            } catch (e: Exception) {
                Log.e("Remmi", "Failed to save recipe image", e)
            }
        }

        val recipe = RecipeItem(
            id = id,
            created = now,
            modified = now,
            title = title,
            description = description,
            imagePath = savedImagePath,
            servings = servings,
            prepTime = prepTime,
            cookingTime = cookingTime,
            ovenTime = ovenTime,
            restingTime = restingTime,
            totalIngredientIds = selectedIngredients.map { it.metadata.id },
            steps = steps,
            mealType = MealType.OTHER
        )
        
        scope.launch {
            actions.addRecipe(recipe)
            onBack()
        }
    }

    RemmiAddScreen(
        title = "New Recipe",
        onBack = onBack,
        onSave = { onSave() },
        saveEnabled = title.isNotBlank()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Recipe Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(48.dp))
                        Text("Add Photo", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Servings", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (servings > 1) servings-- }) {
                        Icon(Icons.Default.Remove, null)
                    }
                    Text("$servings", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(onClick = { servings++ }) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Time: $totalTime min", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TimeInputItem(Icons.Default.Timer, "Prep", prepTime) { prepTime = it }
                        TimeInputItem(Icons.Default.OutdoorGrill, "Cook", cookingTime) { cookingTime = it }
                        TimeInputItem(Icons.Default.SoupKitchen, "Oven", ovenTime) { ovenTime = it }
                        TimeInputItem(Icons.Default.HourglassEmpty, "Rest", restingTime) { restingTime = it }
                    }
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ingredients", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { showIngredientPicker = true }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Select")
                    }
                }
                
                if (selectedIngredients.isEmpty()) {
                    Text("No ingredients selected.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedIngredients.forEach { ing ->
                            InputChip(
                                selected = true,
                                onClick = { selectedIngredients = selectedIngredients - ing },
                                label = { Text(ing.metadata.name) },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
                
                TextButton(onClick = { showAddIngredientDialog = true }) {
                    Text("Can't find it? Add new ingredient", style = MaterialTheme.typography.labelSmall)
                }
            }

            Text("Steps", style = MaterialTheme.typography.titleMedium)

            steps.forEachIndexed { index, step ->
                StepItem(
                    step = step,
                    index = index,
                    availableIngredients = selectedIngredients.toList(),
                    onUpdate = { updatedStep ->
                        val newSteps = steps.toMutableList()
                        newSteps[index] = updatedStep
                        steps = newSteps
                    },
                    onRemove = {
                        if (steps.size > 1) {
                            steps = steps.toMutableList().apply { removeAt(index) }
                        }
                    }
                )
            }

            Button(
                onClick = { steps = steps + RecipeStep(steps.size + 1, "") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Step")
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showIngredientPicker && ingredientActions != null) {
        IngredientPickerDialog(
            actions = ingredientActions,
            currentlySelected = selectedIngredients,
            onDismiss = { showIngredientPicker = false },
            onConfirm = { 
                selectedIngredients = it
                showIngredientPicker = false
            }
        )
    }

    if (showAddIngredientDialog && ingredientActions != null) {
        AddIngredientDialog(
            actions = ingredientActions,
            onDismiss = { showAddIngredientDialog = false },
            onIngredientAdded = { newIng ->
                selectedIngredients = selectedIngredients + newIng
                showAddIngredientDialog = false
            }
        )
    }
}

@Composable
fun TimeInputItem(icon: ImageVector, label: String, value: Int, onValueChange: (Int) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { showDialog = true }
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text("$value", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }

    if (showDialog) {
        var tempValue by remember { mutableStateOf(value.toString()) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Set $label Time") },
            text = {
                OutlinedTextField(
                    value = tempValue,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) tempValue = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("min") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(tempValue.toIntOrNull() ?: 0)
                    showDialog = false
                }) { Text("OK") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StepItem(
    step: RecipeStep,
    index: Int,
    availableIngredients: List<IngredientUiModel>,
    onUpdate: (RecipeStep) -> Unit,
    onRemove: () -> Unit
) {
    var showIngDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${index + 1}", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text("Step ${index + 1}", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }

            OutlinedTextField(
                value = step.description,
                onValueChange = { onUpdate(step.copy(description = it)) },
                label = { Text("What to do...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            
            Spacer(Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                var timeStr by remember { mutableStateOf(step.approxTimeMinutes.toString()) }
                BasicTextField(
                    value = if (timeStr == "0") "" else timeStr,
                    onValueChange = { 
                        if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                            timeStr = it
                            onUpdate(step.copy(approxTimeMinutes = it.toIntOrNull() ?: 0))
                        }
                    },
                    modifier = Modifier.width(40.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text("min", style = MaterialTheme.typography.bodySmall)
                
                Spacer(Modifier.weight(1f))
                
                TextButton(onClick = { showIngDialog = true }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                    Text("Add Ingredient", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (step.ingredients.isNotEmpty()) {
                FlowRow(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    step.ingredients.forEach { ing ->
                        AssistChip(
                            onClick = { /* Edit amount logic? */ },
                            label = { Text("${ing.name}: ${ing.amount} ${ing.unit}") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    modifier = Modifier.size(14.dp).clickable {
                                        onUpdate(step.copy(ingredients = step.ingredients - ing))
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (showIngDialog) {
        StepIngredientDialog(
            available = availableIngredients,
            onDismiss = { showIngDialog = false },
            onConfirm = { ing ->
                onUpdate(step.copy(ingredients = step.ingredients + ing))
                showIngDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepIngredientDialog(
    available: List<IngredientUiModel>,
    onDismiss: () -> Unit,
    onConfirm: (StepIngredient) -> Unit
) {
    var selectedItem by remember { mutableStateOf<IngredientUiModel?>(null) }
    var amount by remember { mutableStateOf("") }
    
    var unit by remember { mutableStateOf(MeasurementUnit.GRAMS) }
    
    val allowedUnits = remember(selectedItem) {
        selectedItem?.metadata?.allowedUnits?.takeIf { it.isNotEmpty() } 
            ?: listOf(MeasurementUnit.GRAMS, MeasurementUnit.UNITS)
    }

    val conversionPreview = remember(selectedItem, amount, unit) {
        val qty = amount.toDoubleOrNull() ?: 0.0
        if (selectedItem != null && unit != MeasurementUnit.GRAMS) {
            val inGrams = convertUnit(qty, unit, MeasurementUnit.GRAMS, selectedItem!!.metadata)
            val (fQty, fUnit) = formatQuantity(inGrams, MeasurementUnit.GRAMS)
            "(~$fQty $fUnit)"
        } else ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ingredient to Step") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedItem?.metadata?.name ?: "Select Ingredient",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        available.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.metadata.name) },
                                onClick = { 
                                    selectedItem = item
                                    if (!allowedUnits.contains(unit)) {
                                        unit = allowedUnits.firstOrNull() ?: MeasurementUnit.GRAMS
                                    }
                                    expanded = false 
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.isEmpty() || it.replace(".", "").all { c -> c.isDigit() }) amount = it },
                        label = { Text("Amount") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    var unitExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = unit.name.lowercase(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                            allowedUnits.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u.name.lowercase()) },
                                    onClick = { unit = u; unitExpanded = false }
                                )
                            }
                        }
                    }
                }
                
                if (conversionPreview.isNotBlank()) {
                    Text(
                        text = conversionPreview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedItem?.let {
                        onConfirm(StepIngredient(it.metadata.id, it.metadata.name, amount.toDoubleOrNull() ?: 0.0, unit.name.lowercase()))
                    }
                },
                enabled = selectedItem != null && amount.isNotEmpty()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun IngredientPickerDialog(
    actions: IngredientActions,
    currentlySelected: Set<IngredientUiModel>,
    onDismiss: () -> Unit,
    onConfirm: (Set<IngredientUiModel>) -> Unit
) {
    var allIngredients by remember { mutableStateOf(emptyList<IngredientUiModel>()) }
    var tempSelected by remember { mutableStateOf(currentlySelected) }
    
    LaunchedEffect(Unit) {
        allIngredients = actions.getInventory()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Ingredients") },
        text = {
            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                LazyColumn {
                    items(allIngredients.size) { index ->
                        val item = allIngredients[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newSelected = tempSelected.toMutableSet()
                                    if (newSelected.contains(item)) newSelected.remove(item) else newSelected.add(item)
                                    tempSelected = newSelected
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = tempSelected.contains(item), onCheckedChange = null)
                            Spacer(Modifier.width(8.dp))
                            Text(item.metadata.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(tempSelected) }) { Text("Done") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientDialog(
    actions: IngredientActions,
    onDismiss: () -> Unit,
    onIngredientAdded: (IngredientUiModel) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var foodGroup by remember { mutableStateOf(FoodGroup.OTHER) }
    
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Ingredient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name*") }, modifier = Modifier.fillMaxWidth())
                
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = foodGroup.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        FoodGroup.entries.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = { foodGroup = group; expanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        actions.addIngredient(name, foodGroup, 0.0, MeasurementUnit.GRAMS, allowedUnits = listOf(MeasurementUnit.GRAMS, MeasurementUnit.UNITS))
                        val inventory = actions.getInventory()
                        inventory.find { it.metadata.name == name }?.let { onIngredientAdded(it) }
                    }
                },
                enabled = name.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
