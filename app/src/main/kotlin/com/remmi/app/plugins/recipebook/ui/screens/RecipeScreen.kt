package com.remmi.app.plugins.recipebook.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.components.RemmiHomeScreen
import com.remmi.app.ui.components.RemmiFAB
import com.remmi.app.ui.popups.RecipeNutritionRadarGraph
import com.remmi.app.ui.components.RemmiCard
import com.remmi.app.plugins.recipebook.RecipeActions
import com.remmi.app.plugins.recipebook.models.MealType
import com.remmi.app.plugins.recipebook.models.RecipeItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    actions: RecipeActions,
    controller: RemmiController
) {
    Log.d("Remmi", "[RecipeScreen] - [RecipeScreen] executed")
    val scope = rememberCoroutineScope()
    var recipes by remember { mutableStateOf(emptyList<RecipeItem>()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isAddingRecipe by remember { mutableStateOf(false) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf<MealType?>(null) }
    var selectedIngredient by remember { mutableStateOf<String?>(null) }
    
    var selectedRecipeForDetail by remember { mutableStateOf<RecipeItem?>(null) }

    val onRefresh: () -> Unit = remember {
        {
            scope.launch {
                isRefreshing = true
                recipes = actions.getAllRecipes()
                delay(500)
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        recipes = actions.getAllRecipes()
    }

    val allIngredients = remember(recipes) {
        recipes.flatMap { it.ingredients }.map { it.name }.distinct().sorted()
    }

    val filteredRecipes = remember(recipes, searchQuery, selectedMealType, selectedIngredient) {
        recipes.filter { recipe ->
            val matchesName = recipe.title.contains(searchQuery, ignoreCase = true)
            val matchesMeal = selectedMealType == null || recipe.mealType == selectedMealType
            val matchesIngredient = selectedIngredient == null || recipe.ingredients.any { it.name == selectedIngredient }
            matchesName && matchesMeal && matchesIngredient
        }
    }

    if (isAddingRecipe) {
        AddRecipeScreen(
            actions = actions,
            controller = controller,
            onBack = { 
                isAddingRecipe = false
                onRefresh() 
            }
        )
    } else {
        RemmiHomeScreen(
            title = "Recipes",
            floatingActionButton = {
                RemmiFAB(
                    onClick = { isAddingRecipe = true },
                    icon = Icons.Default.Add,
                    modifier = Modifier.padding(bottom = 16.dp),
                    contentDescription = "Add Recipe"
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header with Search and Filters
                HeaderSection(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedMealType = selectedMealType,
                    onMealTypeChange = { selectedMealType = it },
                    selectedIngredient = selectedIngredient,
                    onIngredientChange = { selectedIngredient = it },
                    allIngredients = allIngredients
                )

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (filteredRecipes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (recipes.isEmpty()) "No recipes yet." else "No matches found.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredRecipes, key = { it.id }) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = { selectedRecipeForDetail = recipe }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedRecipeForDetail?.let { recipe ->
        RecipeDetailDialog(
            recipe = recipe,
            onDismiss = { selectedRecipeForDetail = null }
        )
    }
}

@Composable
fun RecipeDetailDialog(
    recipe: RecipeItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(recipe.title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nutrition (Per Serving)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${recipe.nutritionPerServing.calories?.toInt() ?: "--"} kcal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                RecipeNutritionRadarGraph(nutrition = recipe.nutritionPerServing)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    NutritionSnippet("Prot", "${recipe.nutritionPerServing.proteins ?: "--"}g", Modifier.weight(1f))
                    NutritionSnippet("Carbs", "${recipe.nutritionPerServing.carbohydrates ?: "--"}g", Modifier.weight(1f))
                    NutritionSnippet("Fat", "${recipe.nutritionPerServing.fats ?: "--"}g", Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun HeaderSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedMealType: MealType?,
    onMealTypeChange: (MealType?) -> Unit,
    selectedIngredient: String?,
    onIngredientChange: (String?) -> Unit,
    allIngredients: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Left: Meal Type Filter
            var mealMenuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { mealMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Meal Type",
                        tint = if (selectedMealType != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = mealMenuExpanded, onDismissRequest = { mealMenuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("All Meals") },
                        onClick = { onMealTypeChange(null); mealMenuExpanded = false }
                    )
                    MealType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = { onMealTypeChange(type); mealMenuExpanded = false }
                        )
                    }
                }
            }

            // Top Center: Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                placeholder = { Text("Search recipes...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                shape = CircleShape,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            // Top Right: Ingredient Filter
            var ingMenuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { ingMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Ingredients",
                        tint = if (selectedIngredient != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = ingMenuExpanded, onDismissRequest = { ingMenuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("All Ingredients") },
                        onClick = { onIngredientChange(null); ingMenuExpanded = false }
                    )
                    allIngredients.forEach { ingredient ->
                        DropdownMenuItem(
                            text = { Text(ingredient) },
                            onClick = { onIngredientChange(ingredient); ingMenuExpanded = false }
                        )
                    }
                }
            }
        }
        
        // Active Filter Chips
        if (selectedMealType != null || selectedIngredient != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedMealType?.let { type ->
                    InputChip(
                        selected = true,
                        onClick = { onMealTypeChange(null) },
                        label = { Text(type.name) },
                        trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                    )
                }
                selectedIngredient?.let { ing ->
                    InputChip(
                        selected = true,
                        onClick = { onIngredientChange(null) },
                        label = { Text(ing) },
                        trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: RecipeItem,
    onClick: () -> Unit
) {
    RemmiCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = recipe.mealType.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutritionSnippet("PROT", "${recipe.nutritionPerServing.proteins ?: "--"}g")
                NutritionSnippet("CARBS", "${recipe.nutritionPerServing.carbohydrates ?: "--"}g")
                NutritionSnippet("FAT", "${recipe.nutritionPerServing.fats ?: "--"}g")
                NutritionSnippet("KCAL", "${recipe.nutritionPerServing.calories?.toInt() ?: "--"}")
            }
        }
    }
}

@Composable
fun NutritionSnippet(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
