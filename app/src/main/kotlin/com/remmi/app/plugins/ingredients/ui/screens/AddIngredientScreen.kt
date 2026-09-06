package com.remmi.app.plugins.ingredients.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.remmi.app.ui.popups.RemmiDatePickerDialog
import com.remmi.app.plugins.ingredients.IngredientActions
import com.remmi.app.plugins.ingredients.models.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@Composable
fun AddIngredientScreen(
    actions: IngredientActions,
    onBack: () -> Unit,
    onRegisterNew: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var metadataList by remember { mutableStateOf<List<IngredientMetadata>>(emptyList()) }
    var shops by remember { mutableStateOf<List<Shop>>(emptyList()) }

    var selectedMetadata by remember { mutableStateOf<IngredientMetadata?>(null) }
    var nameQuery by remember { mutableStateOf("") }
    
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(MeasurementUnit.GRAMS) }
    var expiryDate by remember { mutableStateOf<LocalDate?>(null) }
    var foodGroup by remember { mutableStateOf(FoodGroup.VEGETABLES) }
    
    var selectedShop by remember { mutableStateOf<Shop?>(null) }
    var price by remember { mutableStateOf("") }
    
    var showNewShopDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Measurement scaling state
    val allowedUnitsMap = remember { mutableStateMapOf<MeasurementUnit, Boolean>().apply {
        MeasurementUnit.entries.forEach { put(it, it == MeasurementUnit.GRAMS) }
    } }
    var unitsAmount by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        metadataList = actions.getMetadataList()
        shops = actions.getShops()
    }

    RemmiAddScreen(
        title = "Add to Stock",
        onBack = onBack,
        onSave = {
            scope.launch {
                val finalName = selectedMetadata?.name ?: nameQuery
                val conversions = mutableListOf<IngredientConversion>()
                
                // If we are adding units, add conversion if selected
                if (allowedUnitsMap[MeasurementUnit.UNITS] == true && unitsAmount.isNotEmpty()) {
                    // Logic to add conversion or handle scaling is in actions.addIngredient
                }

                actions.addIngredient(
                    name = finalName,
                    foodGroup = foodGroup,
                    initialQuantity = quantity.toDoubleOrNull() ?: 0.0,
                    unit = unit,
                    expiryDate = expiryDate,
                    allowedUnits = allowedUnitsMap.filter { it.value }.keys.toList(),
                    shopId = selectedShop?.id,
                    price = price.toDoubleOrNull(),
                    baseNutrition = selectedMetadata?.baseNutrition,
                    shelfLife = (selectedMetadata?.estimatedShelfLifeMinDays ?: 0) to (selectedMetadata?.estimatedShelfLifeMaxDays ?: 0)
                )
                onBack()
            }
        },
        saveEnabled = (selectedMetadata != null || nameQuery.isNotEmpty()) && quantity.isNotEmpty()
    ) {
        // SECTION 1: Name & Selection
        RemmiSectionHeader("Ingredient Info")
        
        var nameExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedMetadata?.name ?: nameQuery,
                onValueChange = { 
                    nameQuery = it
                    selectedMetadata = null
                    nameExpanded = true
                },
                label = { Text("Search or Type Name") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { onRegisterNew(nameQuery) }) {
                        Icon(Icons.Default.AppRegistration, contentDescription = "Register New")
                    }
                },
                shape = CircleShape
            )
            
            val suggestions = metadataList.filter { it.name.contains(nameQuery, ignoreCase = true) }.take(5)
            if (nameExpanded && suggestions.isNotEmpty()) {
                DropdownMenu(
                    expanded = nameExpanded,
                    onDismissRequest = { nameExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    suggestions.forEach { meta ->
                        DropdownMenuItem(
                            text = { Text(meta.name) },
                            onClick = {
                                selectedMetadata = meta
                                nameQuery = meta.name
                                foodGroup = meta.foodGroup
                                nameExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = quantity,
                onValueChange = { if (it.toDoubleOrNull() != null || it.isEmpty()) quantity = it },
                label = { Text("Quantity") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = CircleShape
            )
            
            var unitExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = unit.name.lowercase(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unit") },
                    trailingIcon = { IconButton(onClick = { unitExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape
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

        OutlinedTextField(
            value = expiryDate?.toString() ?: "No Expiry",
            onValueChange = {},
            readOnly = true,
            label = { Text("Expiry Date") },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarMonth, null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape
        )

        // SECTION 2: Shop & Price
        RemmiSectionHeader("Purchase Details")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var shopExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = selectedShop?.name ?: "Select Shop",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Shop") },
                    trailingIcon = { IconButton(onClick = { shopExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape
                )
                DropdownMenu(expanded = shopExpanded, onDismissRequest = { shopExpanded = false }) {
                    shops.forEach { shop ->
                        DropdownMenuItem(
                            text = { Text(shop.name) },
                            onClick = { selectedShop = shop; shopExpanded = false }
                        )
                    }
                }
            }
            
            IconButton(
                onClick = { showNewShopDialog = true },
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.AddHome, contentDescription = "Add Shop")
            }
        }

        OutlinedTextField(
            value = price,
            onValueChange = { if (it.toDoubleOrNull() != null || it.isEmpty()) price = it },
            label = { Text("Price (Total)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = CircleShape,
            leadingIcon = { Text("$", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) }
        )

        // SECTION 3: Measurements
        RemmiSectionHeader("Add Measurements")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MeasurementUnit.entries.forEach { u ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = allowedUnitsMap[u] ?: false,
                        onCheckedChange = { allowedUnitsMap[u] = it }
                    )
                    Text(u.name.lowercase(), modifier = Modifier.weight(1f))
                    
                    if (allowedUnitsMap[u] == true && u == MeasurementUnit.UNITS) {
                        OutlinedTextField(
                            value = unitsAmount,
                            onValueChange = { unitsAmount = it },
                            label = { Text("Weight per unit (g)") },
                            modifier = Modifier.width(150.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = CircleShape
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
    }

    if (showDatePicker) {
        val today = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        RemmiDatePickerDialog(
            initialDate = expiryDate ?: today,
            onDismiss = { showDatePicker = false },
            onDateSelected = { expiryDate = it; showDatePicker = false }
        )
    }

    if (showNewShopDialog) {
        var newShopName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewShopDialog = false },
            title = { Text("Add New Shop") },
            text = {
                OutlinedTextField(
                    value = newShopName,
                    onValueChange = { newShopName = it },
                    label = { Text("Shop Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val shop = actions.addShop(newShopName)
                            shops = actions.getShops()
                            selectedShop = shop
                            showNewShopDialog = false
                        }
                    },
                    enabled = newShopName.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewShopDialog = false }) { Text("Cancel") }
            }
        )
    }
}
