package com.remmi.app.plugins.contacts.screens

import com.remmi.app.plugins.contacts.ContactActions
import com.remmi.app.plugins.contacts.ContactItem

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(actions: ContactActions, controller: RemmiController) {
    Log.d("Remmi", "[ContactScreen] - [ContactScreen] executed")
    val scope = rememberCoroutineScope()
    var contacts by remember { mutableStateOf(emptyList<ContactItem>()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroupFilter by remember { mutableStateOf("All") }
    
    var selectedContact by remember { mutableStateOf<ContactItem?>(null) }
    var editorMode by remember { mutableStateOf<ContactEditorMode?>(null) }
    
    // Track editor state for hiding bottom menu
    LaunchedEffect(editorMode, selectedContact) {
        com.remmi.app.core.controller.GlobalUIState.isEditorActive.value = editorMode != null || selectedContact != null
    }

    DisposableEffect(Unit) {
        onDispose {
            com.remmi.app.core.controller.GlobalUIState.isEditorActive.value = false
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    val onRefresh: () -> Unit = remember {
        {
            scope.launch {
                isRefreshing = true
                contacts = actions.getAllContacts()
                delay(500)
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        contacts = actions.getAllContacts()
    }

    val existingGroups = remember(contacts) {
        listOf("All") + contacts.map { it.group }.distinct().sorted()
    }

    val filteredContacts = remember(contacts, searchQuery, selectedGroupFilter) {
        contacts.filter {
            (selectedGroupFilter == "All" || it.group == selectedGroupFilter) &&
            (it.name.contains(searchQuery, ignoreCase = true) ||
                    it.surname.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editorMode = ContactEditorMode.Create },
                modifier = Modifier.padding(bottom = 224.dp) // Above search and menu
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        },
        bottomBar = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 160.dp), // Above island menu
                placeholder = { Text("Search by name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = CircleShape,
                singleLine = true
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "My Contacts",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center)
                    )
                    
                    var isFilterExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)) {
                        IconButton(onClick = { isFilterExpanded = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                        DropdownMenu(
                            expanded = isFilterExpanded,
                            onDismissRequest = { isFilterExpanded = false }
                        ) {
                            existingGroups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group) },
                                    onClick = {
                                        selectedGroupFilter = group
                                        isFilterExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (filteredContacts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No contacts found.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 180.dp)
                    ) {
                        items(filteredContacts, key = { it.id }) { contact ->
                            ContactRow(
                                contact = contact,
                                onToggleFavorite = {
                                    scope.launch {
                                        actions.toggleFavorite(contact)
                                        contacts = actions.getAllContacts()
                                    }
                                },
                                onClick = { selectedContact = contact },
                                onEdit = { editorMode = ContactEditorMode.Edit(contact) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    if (selectedContact != null) {
        ContactDetailScreen(
            contact = selectedContact!!,
            onToggleGiftList = {
                scope.launch {
                    actions.toggleGiftList(selectedContact!!)
                    contacts = actions.getAllContacts()
                    selectedContact = contacts.find { it.id == selectedContact!!.id }
                }
            },
            onDismiss = { selectedContact = null }
        )
    }

    // Editor Dialog
    if (editorMode != null) {
        ContactEditorScreen(
            mode = editorMode!!,
            actions = actions,
            onDismiss = { editorMode = null },
            onSave = {
                scope.launch {
                    contacts = actions.getAllContacts()
                    editorMode = null
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactRow(
    contact: ContactItem,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    Log.d("Remmi", "[ContactScreen] - [ContactRow] executed")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onEdit
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                val displayName = if (!contact.nickname.isNullOrEmpty()) {
                    "${contact.name} ${contact.surname} (${contact.nickname})"
                } else {
                    "${contact.name} ${contact.surname}"
                }
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = contact.group,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (contact.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (contact.isFavorite) Color.Red else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

sealed class ContactEditorMode {
    init {
        Log.d("Remmi", "[ContactEditorMode] - [constructor] executed")
    }
    data object Create : ContactEditorMode()
    data class Edit(val contact: ContactItem) : ContactEditorMode()
}
