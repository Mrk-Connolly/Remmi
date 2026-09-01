package com.remmi.app.plugins.contacts.ui.screens

import com.remmi.app.plugins.contacts.ContactActions
import com.remmi.app.plugins.contacts.models.ContactItem

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
import com.remmi.app.ui.components.RemmiAddScreen
import com.remmi.app.ui.components.RemmiModifyScreen
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

    var contacts by remember { mutableStateOf<List<ContactItem>>(emptyList()) }
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

    val onSaveAction = {
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

    if (initialContact == null) {
        RemmiAddScreen(
            title = "Add Contact",
            onBack = onDismiss,
            onSave = { onSaveAction() },
            saveEnabled = name.isNotBlank()
        ) {
            EditorContent(
                name = name, onNameChange = { name = it },
                surname = surname, onSurnameChange = { surname = it },
                nickname = nickname, onNicknameChange = { nickname = it },
                phone = phone, onPhoneChange = { phone = it },
                email = email, onEmailChange = { email = it },
                birthday = birthday, onShowDatePicker = { showDatePicker = true },
                group = group, onGroupChange = { group = it },
                isGroupExpanded = isGroupExpanded, onGroupExpandedChange = { isGroupExpanded = it },
                existingGroups = existingGroups,
                onShowAddGroupDialog = { showAddGroupDialog = true },
                inGiftList = inGiftList, onInGiftListChange = { inGiftList = it },
                isFavorite = isFavorite, onIsFavoriteChange = { isFavorite = it }
            )
        }
    } else {
        RemmiModifyScreen(
            title = "Edit Contact",
            onBack = onDismiss,
            onDelete = {
                scope.launch {
                    actions.deleteContact(initialContact.id)
                    onSave()
                }
            },
            onSave = { onSaveAction() },
            saveEnabled = name.isNotBlank()
        ) {
            EditorContent(
                name = name, onNameChange = { name = it },
                surname = surname, onSurnameChange = { surname = it },
                nickname = nickname, onNicknameChange = { nickname = it },
                phone = phone, onPhoneChange = { phone = it },
                email = email, onEmailChange = { email = it },
                birthday = birthday, onShowDatePicker = { showDatePicker = true },
                group = group, onGroupChange = { group = it },
                isGroupExpanded = isGroupExpanded, onGroupExpandedChange = { isGroupExpanded = it },
                existingGroups = existingGroups,
                onShowAddGroupDialog = { showAddGroupDialog = true },
                inGiftList = inGiftList, onInGiftListChange = { inGiftList = it },
                isFavorite = isFavorite, onIsFavoriteChange = { isFavorite = it }
            )
        }
    }

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
                            birthday = date.toString()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorContent(
    name: String, onNameChange: (String) -> Unit,
    surname: String, onSurnameChange: (String) -> Unit,
    nickname: String, onNicknameChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    birthday: String?, onShowDatePicker: () -> Unit,
    group: String, onGroupChange: (String) -> Unit,
    isGroupExpanded: Boolean, onGroupExpandedChange: (Boolean) -> Unit,
    existingGroups: List<String>,
    onShowAddGroupDialog: () -> Unit,
    inGiftList: Boolean, onInGiftListChange: (Boolean) -> Unit,
    isFavorite: Boolean, onIsFavoriteChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Name*") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = surname, onValueChange = onSurnameChange, label = { Text("Surname") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nickname, onValueChange = onNicknameChange, label = { Text("Nickname") }, modifier = Modifier.fillMaxWidth())
        
        OutlinedTextField(
            value = phone, 
            onValueChange = onPhoneChange, 
            label = { Text("Phone") }, 
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
        )
        
        OutlinedTextField(
            value = email, 
            onValueChange = onEmailChange, 
            label = { Text("Email") }, 
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
        )
        
        OutlinedTextField(
            value = birthday ?: "",
            onValueChange = { },
            label = { Text("Birthday") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowDatePicker() },
            enabled = false,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = onShowDatePicker) {
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
                onExpandedChange = onGroupExpandedChange,
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
                    onDismissRequest = { onGroupExpandedChange(false) }
                ) {
                    existingGroups.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g) },
                            onClick = {
                                onGroupChange(g)
                                onGroupExpandedChange(false)
                            }
                        )
                    }
                }
            }
            
            IconButton(
                onClick = onShowAddGroupDialog,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Group")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onInGiftListChange(!inGiftList) }) {
                Icon(
                    imageVector = Icons.Default.Redeem,
                    contentDescription = "Gift List",
                    tint = if (inGiftList) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            IconButton(onClick = { onIsFavoriteChange(!isFavorite) }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) androidx.compose.ui.graphics.Color.Red else MaterialTheme.colorScheme.outline
                )
            }
        }
        
        Spacer(Modifier.height(40.dp))
    }
}
