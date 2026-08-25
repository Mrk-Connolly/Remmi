package com.remmi.app.plugins.ingredients.ui.screens

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.plugins.ingredients.IngredientActions
import com.remmi.app.core.model.ingredients.*
import com.remmi.app.plugins.ingredients.ui.popups.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*

enum class IngredientSortOption {
    QUANTITY_LOW, QUANTITY_HIGH, EXPIRY_DATE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientStockScreen(
    actions: IngredientActions,
    controller: RemmiController
) {
    Log.d("Remmi", "[IngredientStockScreen] - Executed")
    val scope = rememberCoroutineScope()
    var inventory by remember { mutableStateOf(emptyList<IngredientUiModel>()) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFoodGroup by remember { mutableStateOf<FoodGroup?>(null) }
    var sortOption by remember { mutableStateOf(IngredientSortOption.EXPIRY_DATE) }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedItemForDetail by remember { mutableStateOf<IngredientUiModel?>(null) }
    var selectedItemForAdjustment by remember { mutableStateOf<IngredientUiModel?>(null) }

    val onRefresh: () -> Unit = remember {
        {
            scope.launch {
                isRefreshing = true
                inventory = actions.getInventory()
                delay(500L)
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        inventory = actions.getInventory()
    }

    val filteredAndSorted = remember(inventory, searchQuery, selectedFoodGroup, sortOption) {
        inventory.filter { item ->
            val matchesName = item.metadata.name.contains(searchQuery, ignoreCase = true)
            val matchesGroup = selectedFoodGroup == null || item.metadata.foodGroup == selectedFoodGroup
            matchesName && matchesGroup
        }.sortedWith { a, b ->
            when (sortOption) {
                IngredientSortOption.QUANTITY_LOW -> a.totalQuantity.compareTo(b.totalQuantity)
                IngredientSortOption.QUANTITY_HIGH -> b.totalQuantity.compareTo(a.totalQuantity)
                IngredientSortOption.EXPIRY_DATE -> {
                    val dateA = a.nearestExpiry ?: LocalDate(9999, 12, 31)
                    val dateB = b.nearestExpiry ?: LocalDate(9999, 12, 31)
                    dateA.compareTo(dateB)
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.padding(bottom = 168.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header Section
            IngredientHeader(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedFoodGroup = selectedFoodGroup,
                onFoodGroupChange = { selectedFoodGroup = it },
                sortOption = sortOption,
                onSortChange = { sortOption = it }
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                if (inventory.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No ingredients in your stock yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else if (filteredAndSorted.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matches found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 180.dp)
                    ) {
                        items(filteredAndSorted, key = { it.stock.id }) { item ->
                            IngredientRow(
                                item = item,
                                onAdjust = { selectedItemForAdjustment = item },
                                onLongClick = { selectedItemForDetail = item }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddIngredientDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, group, qty, unit, expiry, brand, allowedUnits, conversions, nutrition, shelfLife ->
                scope.launch {
                    actions.addIngredient(
                        name = name, 
                        foodGroup = group, 
                        initialQuantity = qty, 
                        unit = unit, 
                        expiryDate = expiry, 
                        brand = brand, 
                        allowedUnits = allowedUnits, 
                        conversions = conversions,
                        baseNutrition = nutrition,
                        shelfLife = shelfLife
                    )
                    inventory = actions.getInventory()
                    showAddDialog = false
                }
            }
        )
    }

    selectedItemForDetail?.let { item ->
        IngredientDetailDialog(
            item = item,
            onDismiss = { selectedItemForDetail = null },
            onDelete = {
                // Implement delete logic if needed
                selectedItemForDetail = null
            }
        )
    }

    selectedItemForAdjustment?.let { item ->
        StockAdjustmentDialog(
            item = item,
            onDismiss = { selectedItemForAdjustment = null },
            onConfirm = { delta, expiry ->
                scope.launch {
                    actions.adjustStock(item.stock.id, delta, expiry)
                    inventory = actions.getInventory()
                    selectedItemForAdjustment = null
                }
            }
        )
    }
}

@Composable
fun IngredientHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFoodGroup: FoodGroup?,
    onFoodGroupChange: (FoodGroup?) -> Unit,
    sortOption: IngredientSortOption,
    onSortChange: (IngredientSortOption) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Left: Food Group
            var groupMenuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { groupMenuExpanded = true }) {
                    Icon(
                        Icons.Default.Category, 
                        contentDescription = "Food Group",
                        tint = if (selectedFoodGroup != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = groupMenuExpanded, onDismissRequest = { groupMenuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("All Groups") },
                        onClick = { onFoodGroupChange(null); groupMenuExpanded = false }
                    )
                    FoodGroup.entries.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = { onFoodGroupChange(group); groupMenuExpanded = false }
                        )
                    }
                }
            }

            // Top Center: Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                placeholder = { Text("Search ingredients...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                shape = CircleShape,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            // Top Right: Sort
            var sortMenuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { sortMenuExpanded = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort")
                }
                DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                    IngredientSortOption.entries.forEach { option ->
                        val label = when (option) {
                            IngredientSortOption.QUANTITY_LOW -> "Quantity: Low to High"
                            IngredientSortOption.QUANTITY_HIGH -> "Quantity: High to Low"
                            IngredientSortOption.EXPIRY_DATE -> "Expiry Date"
                        }
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = { onSortChange(option); sortMenuExpanded = false }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IngredientRow(
    item: IngredientUiModel,
    onAdjust: () -> Unit,
    onLongClick: () -> Unit
) {
    val expiryStatus = getExpiryStatus(item.nearestExpiry)
    val expiryColor = when (expiryStatus) {
        ExpiryStatus.EXPIRED -> MaterialTheme.colorScheme.error
        ExpiryStatus.EXPIRING_SOON -> Color(0xFFFFB300) // Warning Yellow
        ExpiryStatus.NORMAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onLongClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val iconText = item.metadata.icon ?: item.metadata.name.take(1).uppercase()
                    Text(iconText, style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.width(16.dp))

            // Name and Expiry
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.metadata.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                val expiryText = when {
                    item.totalQuantity <= 0 -> "Out of stock"
                    item.nearestExpiry == null -> "No expiry set"
                    expiryStatus == ExpiryStatus.EXPIRED -> "Expired: ${formatDate(item.nearestExpiry!!)}"
                    expiryStatus == ExpiryStatus.EXPIRING_SOON -> "Expires soon: ${formatDate(item.nearestExpiry!!)}"
                    else -> "Expires: ${formatDate(item.nearestExpiry!!)}"
                }
                
                Text(
                    text = expiryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.totalQuantity <= 0) MaterialTheme.colorScheme.error else expiryColor
                )
            }

            // Quantity
            val (formattedQty, formattedUnit) = formatQuantity(item.totalQuantity, item.stock.primaryUnit)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedQty,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (item.totalQuantity <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedUnit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(8.dp))

            // Adjustment Button
            IconButton(
                onClick = onAdjust,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Balance,
                    contentDescription = "Adjust Stock",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

enum class ExpiryStatus { NORMAL, EXPIRING_SOON, EXPIRED }

fun getExpiryStatus(expiry: LocalDate?): ExpiryStatus {
    if (expiry == null) return ExpiryStatus.NORMAL
    val today = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    val daysUntil = today.daysUntil(expiry)
    
    return when {
        daysUntil < 0 -> ExpiryStatus.EXPIRED
        daysUntil <= 3 -> ExpiryStatus.EXPIRING_SOON
        else -> ExpiryStatus.NORMAL
    }
}

fun formatDate(date: LocalDate): String {
    val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return "${date.day} $monthName"
}
