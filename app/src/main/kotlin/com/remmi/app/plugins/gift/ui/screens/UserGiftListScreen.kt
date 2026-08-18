package com.remmi.app.plugins.gift.ui.screens

import com.remmi.app.plugins.gift.GiftActions
import com.remmi.app.plugins.gift.GiftIdea
import com.remmi.app.plugins.gift.GiftEvent
import com.remmi.app.plugins.gift.ui.popups.*

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.plugins.contacts.ContactItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGiftListScreen(
    contact: ContactItem,
    actions: GiftActions,
    onBack: () -> Unit
) {
    Log.d("Remmi", "[UserGiftListScreen] - [UserGiftListScreen] executed")
    var gifts by remember { mutableStateOf(emptyList<GiftIdea>()) }
    var sortBy by remember { mutableStateOf(SortOption.DATE) }
    var filterByEvent by remember { mutableStateOf<GiftEvent?>(null) }
    
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    LaunchedEffect(contact.id) {
        gifts = actions.getGiftIdeasForContact(contact.id)
    }

    val filteredGifts = remember(gifts, sortBy, filterByEvent) {
        var result = if (filterByEvent != null) {
            gifts.filter { it.event == filterByEvent }
        } else {
            gifts
        }

        result = when (sortBy) {
            SortOption.PRICE_ASC -> result.sortedBy { it.price ?: 0.0 }
            SortOption.PRICE_DESC -> result.sortedByDescending { it.price ?: 0.0 }
            SortOption.TITLE_AZ -> result.sortedBy { it.name }
            SortOption.DATE -> result.sortedByDescending { it.created }
        }
        result
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${contact.name}'s Gifts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        bottomBar = {
            Spacer(Modifier.height(96.dp))
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (filteredGifts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No gifts found.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredGifts, key = { it.id }) { gift ->
                        GiftIdeaRow(gift)
                    }
                }
            }
        }
        
        if (showSortMenu) {
            SortMenu(
                current = sortBy,
                onDismiss = { showSortMenu = false },
                onSelect = { sortBy = it; showSortMenu = false }
            )
        }
        
        if (showFilterMenu) {
            FilterMenu(
                current = filterByEvent,
                onDismiss = { showFilterMenu = false },
                onSelect = { filterByEvent = it; showFilterMenu = false }
            )
        }
    }
}

@Composable
fun GiftIdeaRow(gift: GiftIdea) {
    Log.d("Remmi", "[UserGiftListScreen] - [GiftIdeaRow] executed")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = gift.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                gift.price?.let {
                    Text(text = "$${String.format("%.2f", it)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            gift.description?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
            gift.event?.let {
                SuggestionChip(
                    onClick = {},
                    label = { Text(it.name) },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

enum class SortOption {
    PRICE_ASC, PRICE_DESC, TITLE_AZ, DATE
}

@Composable
fun SortMenu(current: SortOption, onDismiss: () -> Unit, onSelect: (SortOption) -> Unit) {
    Log.d("Remmi", "[UserGiftListScreen] - [SortMenu] executed")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort By") },
        text = {
            Column {
                SortMenuItem("Price: Low to High", SortOption.PRICE_ASC, current, onSelect)
                SortMenuItem("Price: High to Low", SortOption.PRICE_DESC, current, onSelect)
                SortMenuItem("Title: A to Z", SortOption.TITLE_AZ, current, onSelect)
                SortMenuItem("Date Added", SortOption.DATE, current, onSelect)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun SortMenuItem(label: String, option: SortOption, current: SortOption, onSelect: (SortOption) -> Unit) {
    Log.d("Remmi", "[UserGiftListScreen] - [SortMenuItem] executed")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        RadioButton(selected = option == current, onClick = { onSelect(option) })
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun FilterMenu(current: GiftEvent?, onDismiss: () -> Unit, onSelect: (GiftEvent?) -> Unit) {
    Log.d("Remmi", "[UserGiftListScreen] - [FilterMenu] executed")
    val events = GiftEvent.entries
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter By Event") },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    RadioButton(selected = current == null, onClick = { onSelect(null) })
                    Text("All Events", modifier = Modifier.padding(start = 8.dp))
                }
                events.forEach { event ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        RadioButton(selected = event == current, onClick = { onSelect(event) })
                        Text(event.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
