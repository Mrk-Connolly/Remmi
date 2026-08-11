package com.remmi.app.plugins.contacts

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(actions: ContactActions) {
    val scope = rememberCoroutineScope()
    var contacts by remember { mutableStateOf(emptyList<ContactItem>()) }
    var searchQuery by remember { mutableStateOf("") }
    
    var selectedContact by remember { mutableStateOf<ContactItem?>(null) }
    var editorMode by remember { mutableStateOf<ContactEditorMode?>(null) }

    LaunchedEffect(Unit) {
        contacts = actions.getAllContacts()
    }

    val filteredContacts = contacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.surname.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { editorMode = ContactEditorMode.Create }) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        },
        bottomBar = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = CircleShape,
                singleLine = true
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "My Contacts",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            if (filteredContacts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No contacts found.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
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

@Composable
fun ContactRow(
    contact: ContactItem,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with 2-second hold to edit
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                val startTime = System.currentTimeMillis()
                                tryAwaitRelease()
                                val duration = System.currentTimeMillis() - startTime
                                if (duration >= 2000) {
                                    onEdit()
                                }
                            }
                        )
                    },
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
    data object Create : ContactEditorMode()
    data class Edit(val contact: ContactItem) : ContactEditorMode()
}
