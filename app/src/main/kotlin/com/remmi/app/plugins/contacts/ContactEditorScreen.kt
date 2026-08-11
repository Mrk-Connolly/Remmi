package com.remmi.app.plugins.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditorScreen(
    mode: ContactEditorMode,
    actions: ContactActions,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val initialContact = (mode as? ContactEditorMode.Edit)?.contact

    var name by remember { mutableStateOf(initialContact?.name ?: "") }
    var surname by remember { mutableStateOf(initialContact?.surname ?: "") }
    var nickname by remember { mutableStateOf(initialContact?.nickname ?: "") }
    var phone by remember { mutableStateOf(initialContact?.mobilePhone ?: "") }
    var email by remember { mutableStateOf(initialContact?.email ?: "") }
    var birthday by remember { mutableStateOf<String?>(initialContact?.birthday) }
    var group by remember { mutableStateOf(initialContact?.group ?: "") }
    var inGiftList by remember { mutableStateOf(initialContact?.inGiftList ?: false) }
    var isFavorite by remember { mutableStateOf(initialContact?.isFavorite ?: false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
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
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                
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

                OutlinedTextField(value = group, onValueChange = { group = it }, label = { Text("Group") }, modifier = Modifier.fillMaxWidth())

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
