package com.remmi.app.plugins.transcriptions.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.components.RemmiCard
import com.remmi.app.ui.components.RemmiHomeScreen
import com.remmi.app.ui.components.RemmiFAB
import com.remmi.app.plugins.transcriptions.TranscriptionsActions
import com.remmi.app.plugins.transcriptions.models.TranscriptionItem
import com.remmi.app.plugins.transcriptions.models.TranscriptionGroup
import kotlinx.coroutines.launch
import kotlinx.datetime.*

enum class TranscriptionViewMode {
    DATE, GROUPS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptionsScreen(
    actions: TranscriptionsActions,
    controller: RemmiController
) {
    var viewMode by remember { mutableStateOf(TranscriptionViewMode.DATE) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var editorMode by remember { mutableStateOf<TranscriptionEditorMode?>(null) }
    val scope = rememberCoroutineScope()

    val transcriptions by actions.getTranscriptions().collectAsState(initial = emptyList())
    val groups by actions.getGroups().collectAsState(initial = emptyList())

    val filteredTranscriptions = remember(transcriptions, searchQuery) {
        if (searchQuery.isBlank()) transcriptions
        else transcriptions.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.description?.contains(searchQuery, ignoreCase = true) == true ||
            it.transcript?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    if (editorMode is TranscriptionEditorMode.Create) {
        NewTranscriptionScreen(
            actions = actions,
            onDismiss = { editorMode = null }
        )
    } else if (editorMode is TranscriptionEditorMode.Detail) {
        TranscriptionDetailScreen(
            item = (editorMode as TranscriptionEditorMode.Detail).item,
            actions = actions,
            onBack = { editorMode = null }
        )
    } else {
        RemmiHomeScreen(
            title = "Transcriptions",
            topBarActions = {
                IconButton(onClick = {
                    val id = "test_" + java.util.UUID.randomUUID().toString().substring(0, 8)
                    val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
                    val testItem = TranscriptionItem(
                        id = id,
                        created = now,
                        modified = now,
                        title = "Test Transcription",
                        status = "RECORDING",
                        startedAt = now
                    )
                    actions.repository.add(testItem)
                    editorMode = TranscriptionEditorMode.Detail(testItem)
                    scope.launch {
                        actions.testTranscription(id)
                    }
                }) {
                    Icon(Icons.Default.BugReport, contentDescription = "Test UI")
                }
                IconButton(onClick = { isSearching = !isSearching }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
            floatingActionButton = {
                RemmiFAB(
                    onClick = { editorMode = TranscriptionEditorMode.Create },
                    icon = Icons.Default.Add,
                    contentDescription = "New transcription"
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                if (isSearching) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { searchQuery = ""; isSearching = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                SecondaryTabRow(
                    selectedTabIndex = viewMode.ordinal,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(
                        selected = viewMode == TranscriptionViewMode.DATE,
                        onClick = { viewMode = TranscriptionViewMode.DATE },
                        text = { Text("Date") }
                    )
                    Tab(
                        selected = viewMode == TranscriptionViewMode.GROUPS,
                        onClick = { viewMode = TranscriptionViewMode.GROUPS },
                        text = { Text("Groups") }
                    )
                }

                if (viewMode == TranscriptionViewMode.DATE) {
                    TranscriptionDateView(filteredTranscriptions, onDetail = { editorMode = TranscriptionEditorMode.Detail(it) })
                } else {
                    TranscriptionGroupView(filteredTranscriptions, groups, onDetail = { editorMode = TranscriptionEditorMode.Detail(it) })
                }
            }
        }
    }
}

@Composable
fun TranscriptionDateView(items: List<TranscriptionItem>, onDetail: (TranscriptionItem) -> Unit) {
    val sorted = items.sortedByDescending { it.created }
    val grouped = remember(sorted) {
        sorted.groupBy { 
            val date = it.created.toLocalDateTime(TimeZone.currentSystemDefault()).date
            date.toString() // Simplified grouping
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        grouped.forEach { (date, itemsOnDate) ->
            item {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(itemsOnDate) { item ->
                TranscriptionRow(item, onClick = { onDetail(item) })
            }
        }
    }
}

@Composable
fun TranscriptionGroupView(items: List<TranscriptionItem>, groups: List<TranscriptionGroup>, onDetail: (TranscriptionItem) -> Unit) {
    val grouped = remember(items, groups) {
        items.groupBy { it.groupId }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        groups.forEach { group ->
            val itemsInGroup = grouped[group.id] ?: emptyList()
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(text = group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            items(itemsInGroup) { item ->
                TranscriptionRow(item, onClick = { onDetail(item) })
            }
        }
        
        val ungrouped = grouped[null] ?: emptyList()
        if (ungrouped.isNotEmpty()) {
            item {
                Text(text = "Ungrouped", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            }
            items(ungrouped) { item ->
                TranscriptionRow(item, onClick = { onDetail(item) })
            }
        }
    }
}

@Composable
fun TranscriptionRow(item: TranscriptionItem, onClick: () -> Unit) {
    RemmiCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = { Text(item.title, fontWeight = FontWeight.Bold) },
            supportingContent = {
                val date = item.created.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val duration = formatDuration(item.durationMillis)
                Text("$date · $duration")
            },
            trailingContent = {
                if (item.status == "RECORDING") {
                    Icon(Icons.Default.Mic, contentDescription = "Recording", tint = MaterialTheme.colorScheme.error)
                } else {
                    Icon(Icons.Default.Description, contentDescription = "Finished", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

fun formatDuration(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours > 0) "${hours}h ${minutes % 60}m" else "${minutes}m ${seconds % 60}s"
}

sealed class TranscriptionEditorMode {
    data object Create : TranscriptionEditorMode()
    data class Detail(val item: TranscriptionItem) : TranscriptionEditorMode()
}
