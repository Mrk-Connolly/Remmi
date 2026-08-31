package com.remmi.app.plugins.gift.popups

import com.remmi.app.plugins.gift.GiftIdea
import com.remmi.app.plugins.gift.GiftActions

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.plugins.contacts.ContactItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftIdeasDialog(
    contact: ContactItem,
    actions: GiftActions,
    onDismiss: () -> Unit
) {
    Log.d("Remmi", "[GiftIdeasDialog] - [GiftIdeasDialog] executed")
    val scope = rememberCoroutineScope()
    var ideas by remember { mutableStateOf(emptyList<GiftIdea>()) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(contact.id) {
        ideas = actions.getGiftIdeasForContact(contact.id)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gifts for ${contact.name}")
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Idea")
                }
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp)) {
                if (ideas.isEmpty()) {
                    Text("No ideas yet.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(ideas, key = { it.id }) { idea ->
                            GiftIdeaRow(
                                idea = idea,
                                onDelete = {
                                    scope.launch {
                                        actions.deleteGiftIdea(idea.id)
                                        ideas = actions.getGiftIdeasForContact(contact.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    )

    if (showAddDialog) {
        AddGiftDialog(
            contactId = contact.id,
            actions = actions,
            onDismiss = { showAddDialog = false },
            onSave = {
                scope.launch {
                    ideas = actions.getGiftIdeasForContact(contact.id)
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun GiftIdeaRow(idea: GiftIdea, onDelete: () -> Unit) {
    Log.d("Remmi", "[GiftIdeasDialog] - [GiftIdeaRow] executed")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = idea.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                idea.event?.let {
                    Text(text = it.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                idea.price?.let {
                    Text(text = "$$it", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
