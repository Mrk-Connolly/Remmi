package com.remmi.app.plugins.contacts.screens

import com.remmi.app.plugins.contacts.ContactActions
import com.remmi.app.plugins.contacts.ContactItem

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.material3.MenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditorScreen(
    mode: ContactEditorMode,
    actions: ContactActions,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Log.d("Remmi", "[ContactEditorScreen] - [ContactEditorScreen] executed")
    val scope = rememberCoroutineScope()
    val initialContact = (mode as? ContactEditorMode.Edit)?.contact

    var name by remember { mutableStateOf(initialContact?.name ?: "") }
    var surname by remember { mutableStateOf(initialContact?.surname ?: "") }
    var nickname by remember { mutableStateOf(initialContact?.nickname ?: "") }
    var phone by remember { mutableStateOf(initialContact?.mobilePhone ?: "") }
    var email by remember { mutableStateOf(initialContact?.email ?: "") }
    var birthday by remember { mutableStateOf<String?>(initialContact?.birthday) }
    var group by remember { mutableStateOf(initialContact?.group ?: "General") }
    var inGiftList by remember { mutableStateOf(initialContact?.inGiftList ?: false) }
    var isFavorite by remember { mutableStateOf(initialContact?.isFavorite ?: false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var contacts by remember { mutableStateOf(emptyList<ContactItem>()) }
    LaunchedEffect(Unit) {
        contacts = actions.getAllContacts()
    }

    val existingGroups = remember(contacts) {
        (listOf("General", "Family", "Friends", "Work") + contacts.map { it.group }).distinct().sorted()
    }
    
    var isGroupExpanded by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    
    val keyboardController = LocalSoftwareKeyboardController.current

    val onSaveCallback = remember {
        {
            keyboardController?.hide()
            scope.launch {
                val finalPhone = phone.takeIf { it.isNotBlank() }
                val finalEmail = email.takeIf { it.isNotBlank() }
                val finalBirthday = birthday?.takeIf { it.isNotBlank() }
                val finalNickname = nickname.takeIf { it.isNotBlank() }

                if (initialContact != null) {
                    actions.updateContact(
                        initialContact.copy(
                            name = name,
                            surname = surname,
                            nickname = finalNickname,
                            mobilePhone = finalPhone,
                            email = finalEmail,
                            birthday = finalBirthday,
                            group = group,
                            inGiftList = inGiftList,
                            isFavorite = isFavorite
                        )
                    )
                } else {
                    actions.createContact(
                        name = name,
                        surname = surname,
                        nickname = finalNickname,
                        phone = finalPhone,
                        email = finalEmail,
                        birthday = finalBirthday,
                        group = group,
                        inGiftList = inGiftList,
                        isFavorite = isFavorite
                    )
                }
                onSave()
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            keyboardController?.hide()
            onDismiss()
        },
        confirmButton = {
            Button(
                onClick = { onSaveCallback() },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                keyboardController?.hide()
                onDismiss()
            }) { Text("Cancel") }
        },
        title = {
            Text(if (initialContact == null) "Add Contact" else "Edit Contact")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name*") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = surname, onValueChange = { surname = it }, label = { Text("Surname") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = nickname, onValueChange = { nickname = it }, label = { Text("Nickname") }, modifier = Modifier.fillMaxWidth())
                
                OutlinedTextField(
                    value = phone, 
                    onValueChange = { phone = it }, 
                    label = { Text("Phone") }, 
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                )
                
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = { Text("Email") }, 
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                )
                
                // Birthday Picker
                OutlinedTextField(
                    value = birthday ?: "",
                    onValueChange = { },
                    label = { Text("Birthday") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    enabled = false,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Cake, contentDescription = "Select Date")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = isGroupExpanded,
                        onExpandedChange = { isGroupExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = group,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Group") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGroupExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isGroupExpanded,
                            onDismissRequest = { isGroupExpanded = false }
                        ) {
                            existingGroups.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g) },
                                    onClick = {
                                        group = g
                                        isGroupExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    IconButton(
                        onClick = { showAddGroupDialog = true },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Group")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { inGiftList = !inGiftList }) {
                        Icon(
                            imageVector = Icons.Default.Redeem,
                            contentDescription = "Gift List",
                            tint = if (inGiftList) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) androidx.compose.ui.graphics.Color.Red else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    )

    if (showAddGroupDialog) {
        AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text("Add New Group") },
            text = {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    label = { Text("Group Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newGroupName.isNotBlank()) {
                        group = newGroupName
                        showAddGroupDialog = false
                        newGroupName = ""
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.UTC).date
                            birthday = date.toString() // YYYY-MM-DD
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
