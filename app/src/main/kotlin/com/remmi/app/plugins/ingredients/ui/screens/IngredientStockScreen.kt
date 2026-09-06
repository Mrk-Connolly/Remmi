package com.remmi.app.plugins.ingredients.ui.screens

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.components.RemmiHomeScreen
import com.remmi.app.ui.components.RemmiCard
import com.remmi.app.plugins.ingredients.IngredientActions
import com.remmi.app.plugins.ingredients.models.*
import com.remmi.app.plugins.ingredients.ui.popups.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*

enum class IngredientSortOption {
    QUANTITY_LOW, QUANTITY_HIGH, EXPIRY_DATE
}

enum class IngredientScreenState {
    LIST, ADD, REGISTER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientStockScreen(
    actions: IngredientActions,
    controller: RemmiController
) {
    Log.d("Remmi", "[IngredientStockScreen] - Executed")
    val scope = rememberCoroutineScope()
    var inventory by remember { mutableStateOf<List<IngredientUiModel>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    var screenState by remember { mutableStateOf(IngredientScreenState.LIST) }
    var registerInitialName by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFoodGroup by remember { mutableStateOf<FoodGroup?>(null) }
    var sortOption by remember { mutableStateOf(IngredientSortOption.EXPIRY_DATE) }

    var selectedItemForDetail by remember { mutableStateOf<IngredientUiModel?>(null) }
    var selectedItemForAdjustment by remember { mutableStateOf<IngredientUiModel?>(null) }

    var showScanResults by remember { mutableStateOf(false) }
    var scanResults by remember { mutableStateOf<List<ReceiptItemMatch>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        controller.eventBus.events.collect { event ->
            if (event is com.remmi.app.core.eventBus.events.ReceiptTextRecognizedEvent) {
                val matches = actions.processRecognizedText(event.text)
                scanResults = matches
                showScanResults = true
                isScanning = false
            }
        }
    }

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

    LaunchedEffect(screenState) {
        if (screenState == IngredientScreenState.LIST) {
            inventory = actions.getInventory()
        }
    }

    when (screenState) {
        IngredientScreenState.ADD -> {
            AddIngredientScreen(
                actions = actions,
                onBack = { screenState = IngredientScreenState.LIST },
                onRegisterNew = { name -> 
                    registerInitialName = name
                    screenState = IngredientScreenState.REGISTER 
                }
            )
        }
        IngredientScreenState.REGISTER -> {
            RegisterIngredientScreen(
                actions = actions,
                initialName = registerInitialName,
                onBack = { screenState = IngredientScreenState.ADD }
            )
        }
        IngredientScreenState.LIST -> {
            StockListScreen(
                inventory = inventory,
                isRefreshing = isRefreshing,
                isScanning = isScanning,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedFoodGroup = selectedFoodGroup,
                onFoodGroupChange = { selectedFoodGroup = it },
                sortOption = sortOption,
                onSortChange = { sortOption = it },
                onRefresh = onRefresh,
                onAdd = { screenState = IngredientScreenState.ADD },
                onAdjust = { selectedItemForAdjustment = it },
                onDetail = { selectedItemForDetail = it },
                onScanReceipt = { useCamera ->
                    scope.launch { actions.startReceiptScan(useCamera) }
                    isScanning = true
                }
            )
        }
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

    if (showScanResults) {
        ReceiptScanResultsPopup(
            results = scanResults,
            onDismiss = { showScanResults = false },
            onConfirm = { confirmedItems ->
                scope.launch {
                    actions.processConfirmedReceiptItems(confirmedItems)
                    inventory = actions.getInventory()
                    showScanResults = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    inventory: List<IngredientUiModel>,
    isRefreshing: Boolean,
    isScanning: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFoodGroup: FoodGroup?,
    onFoodGroupChange: (FoodGroup?) -> Unit,
    sortOption: IngredientSortOption,
    onSortChange: (IngredientSortOption) -> Unit,
    onRefresh: () -> Unit,
    onAdd: () -> Unit,
    onAdjust: (IngredientUiModel) -> Unit,
    onDetail: (IngredientUiModel) -> Unit,
    onScanReceipt: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
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

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.background
        )
    )

    RemmiHomeScreen(
        title = "",
        backgroundBrush = backgroundBrush,
        floatingActionButton = {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    var showScanOptions by remember { mutableStateOf(false) }
                    
                    if (showScanOptions) {
                        SmallFloatingActionButton(
                            onClick = { 
                                onScanReceipt(true)
                                showScanOptions = false
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Camera")
                        }
                        SmallFloatingActionButton(
                            onClick = { 
                                onScanReceipt(false)
                                showScanOptions = false
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Gallery")
                        }
                    }

                    FloatingActionButton(
                        onClick = { showScanOptions = !showScanOptions },
                        modifier = Modifier.padding(bottom = 16.dp),
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(if (showScanOptions) Icons.Default.Close else Icons.Default.Receipt, contentDescription = "Scan Receipt")
                    }
                    
                    FloatingActionButton(
                        onClick = onAdd,
                        modifier = Modifier.padding(bottom = 16.dp),
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header Section
            IngredientHeader(
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                selectedFoodGroup = selectedFoodGroup,
                onFoodGroupChange = onFoodGroupChange,
                sortOption = sortOption,
                onSortChange = onSortChange
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing || isScanning,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isScanning) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (inventory.isEmpty()) {
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
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredAndSorted, key = { it.stock.id }) { item ->
                            IngredientRow(
                                item = item,
                                onAdjust = { onAdjust(item) },
                                onLongClick = { onDetail(item) }
                            )
                        }
                    }
                }
            }
        }
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
                        leadingIcon = { Icon(Icons.Default.List, contentDescription = null) },
                        onClick = { onFoodGroupChange(null); groupMenuExpanded = false }
                    )
                    FoodGroup.entries.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
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

    RemmiCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onLongClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val iconText = item.metadata.icon ?: item.metadata.name.take(1).uppercase()
                    Text(
                        iconText,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Name and Expiry
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.metadata.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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
            val (formattedQty, formattedUnit) = formatQuantity(
                item.totalQuantity,
                item.stock.primaryUnit
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedQty,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (item.totalQuantity <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedUnit.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Adjustment Button
            IconButton(
                onClick = onAdjust,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Balance,
                    contentDescription = "Adjust Stock",
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
