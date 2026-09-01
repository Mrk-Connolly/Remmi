package com.remmi.app.plugins.gift.ui.popups

import com.remmi.app.plugins.gift.GiftActions
import com.remmi.app.plugins.gift.models.GiftEvent

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.remmi.app.ui.components.RemmiTitleDescriptionGroup
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGiftDialog(
    contactId: String,
    actions: GiftActions,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Log.d("Remmi", "[AddGiftDialog] - Refactored")
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedEvent by remember { mutableStateOf<GiftEvent?>(null) }
    var showEventDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        actions.addGiftIdea(
                            contactId = contactId,
                            name = name,
                            description = description.takeIf { it.isNotBlank() },
                            link = link.takeIf { it.isNotBlank() },
                            price = price.toDoubleOrNull(),
                            event = selectedEvent
                        )
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
        title = { Text("New Gift Idea") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RemmiTitleDescriptionGroup(
                    title = name,
                    onTitleChange = { name = it },
                    description = description,
                    onDescriptionChange = { description = it }
                )

                OutlinedTextField(value = link, onValueChange = { link = it }, label = { Text("Link") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showEventDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedEvent?.name ?: "Select Event")
                    }
                    DropdownMenu(
                        expanded = showEventDropdown,
                        onDismissRequest = { showEventDropdown = false }
                    ) {
                        GiftEvent.entries.forEach { event ->
                            DropdownMenuItem(
                                text = { Text(event.name) },
                                onClick = {
                                    selectedEvent = event
                                    showEventDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}
