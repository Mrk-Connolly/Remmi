package com.remmi.app.core.screens.components.popups

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.plugins.contacts.ContactItem

/**
 * CONTACTS SELECTION DIALOG
 * Shared component for selecting multiple participants from the contact list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsSelectionDialog(
    contacts: List<ContactItem>,
    selectedParticipants: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    Log.d("Remmi", "[ContactsSelectionDialog] - Generic")
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("All") }
    var isGroupDropdownExpanded by remember { mutableStateOf(false) }
    
    val currentSelected = remember { mutableStateListOf<String>().apply { addAll(selectedParticipants) } }

    val groups = listOf("All") + contacts.map { it.group }.distinct()
    val filteredContacts = contacts.filter { 
        (selectedGroup == "All" || it.group == selectedGroup) &&
        (it.name.contains(searchQuery, ignoreCase = true) || it.surname.contains(searchQuery, ignoreCase = true))
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Participants") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                
                ExposedDropdownMenuBox(
                    expanded = isGroupDropdownExpanded,
                    onExpandedChange = { isGroupDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedGroup,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Group") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGroupDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isGroupDropdownExpanded,
                        onDismissRequest = { isGroupDropdownExpanded = false }
                    ) {
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group) },
                                onClick = {
                                    selectedGroup = group
                                    isGroupDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(filteredContacts) { contact ->
                        val fullName = "${contact.name} ${contact.surname}"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (currentSelected.contains(fullName)) currentSelected.remove(fullName)
                                else currentSelected.add(fullName)
                            }
                        ) {
                            Checkbox(
                                checked = currentSelected.contains(fullName),
                                onCheckedChange = {
                                    if (it) currentSelected.add(fullName)
                                    else currentSelected.remove(fullName)
                                }
                            )
                            Text("${contact.name} ${contact.surname} (${contact.group})")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(currentSelected.toList()) }) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
