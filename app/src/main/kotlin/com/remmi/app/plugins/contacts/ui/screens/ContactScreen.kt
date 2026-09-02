package com.remmi.app.plugins.contacts.ui.screens

import com.remmi.app.plugins.contacts.ContactActions
import com.remmi.app.plugins.contacts.models.ContactItem

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.components.RemmiHomeScreen
import com.remmi.app.ui.components.RemmiFAB
import com.remmi.app.ui.components.RemmiCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(actions: ContactActions, controller: RemmiController) {
    Log.d("Remmi", "[ContactScreen] - [ContactScreen] executed")
    val scope = rememberCoroutineScope()
    var contacts by remember { mutableStateOf<List<ContactItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroupFilter by remember { mutableStateOf("All") }
    
    var selectedContact by remember { mutableStateOf<ContactItem?>(null) }
    var editorMode by remember { mutableStateOf<ContactEditorMode?>(null) }
    
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
    } else {
        RemmiHomeScreen(
            title = "Contacts",
            floatingActionButton = {
                RemmiFAB(
                    onClick = { editorMode = ContactEditorMode.Create },
                    icon = Icons.Default.Add,
                    modifier = Modifier.padding(bottom = 16.dp),
                    contentDescription = "Add Contact"
                )
            }
        ) { padding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
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
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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
    RemmiCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onEdit
            )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = contact.group,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(
                onClick = onToggleFavorite,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (contact.isFavorite) Color.Red else MaterialTheme.colorScheme.outline
                )
            ) {
                Icon(
                    imageVector = if (contact.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    modifier = Modifier.size(22.dp)
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
