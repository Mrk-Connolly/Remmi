package com.remmi.app.plugins.calendar.ui.popups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.screens.components.RemmiColorPicker
import com.remmi.app.plugins.calendar.models.CalendarGroup

import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import com.remmi.app.core.screens.components.RemmiColorPicker

/**
 * Dialog for creating a new calendar group with a name and color picker.
 */
@Composable
fun NewGroupDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#6200EE") }
    var showColorPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Group", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Text("Select Color", style = MaterialTheme.typography.labelMedium)
                
                // Color Picker Trigger
                OutlinedCard(
                    onClick = { showColorPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(android.graphics.Color.parseColor(selectedColor)), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(text = "Choose Color & Style", modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ColorLens, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                // Preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Preview:", style = MaterialTheme.typography.labelSmall)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(android.graphics.Color.parseColor(selectedColor)), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    )
                    Text(
                        text = name.ifEmpty { "Group Name" }, 
                        color = Color(android.graphics.Color.parseColor(selectedColor)), 
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, selectedColor) },
                enabled = name.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showColorPicker) {
        RemmiColorPicker(
            initialColor = selectedColor,
            onDismiss = { showColorPicker = false },
            onColorSelected = { selectedColor = it }
        )
    }
}

/**
 * Popup for selecting participants from contacts.
 */
@Composable
fun ParticipantsPopup(
    onDismiss: () -> Unit,
    onConfirmed: (List<String>) -> Unit,
    initialParticipants: List<String> = emptyList()
) {
    var selectedParticipants by remember { mutableStateOf(initialParticipants.toMutableList()) }
    
    // Mock contacts for now, in a real app these would come from the ContactPlugin
    val mockContacts = listOf("Alice Smith", "Bob Jones", "Charlie Brown", "Diana Prince", "Edward Norton")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Participants", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                mockContacts.forEach { contact ->
                    val isSelected = selectedParticipants.contains(contact)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) selectedParticipants.remove(contact)
                                else selectedParticipants.add(contact)
                                // Trigger recomposition manually because we are using toMutableList().
                                // Better to use mutableStateListOf but this works for now if we clone it.
                                selectedParticipants = selectedParticipants.toMutableList()
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(text = contact, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Checkbox(checked = isSelected, onCheckedChange = null)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmed(selectedParticipants); onDismiss() },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm (${selectedParticipants.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
