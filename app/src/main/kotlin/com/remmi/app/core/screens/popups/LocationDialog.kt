package com.remmi.app.core.screens.popups

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * LOCATION DIALOG
 * Shared component for managing multiple locations associated with an item
 */
@Composable
fun LocationDialog(
    initialLocations: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    Log.d("Remmi", "[LocationDialog] - [LocationDialog] executed")
    var name by remember { mutableStateOf("") }
    val currentLocations = remember { mutableStateListOf<String>().apply { addAll(initialLocations) } }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Location Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                currentLocations.forEach { loc ->
                    Text(text = loc, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Row {
                /**                                 Add
                 * Add a location string to the temporary list
                 * */
                TextButton(onClick = { if (name.isNotBlank()) currentLocations.add(name); name = "" }) { 
                    Text("Add") 
                }

                /**                                 Confirm
                 * Confirm the full list of locations and close
                 * */
                Button(onClick = { onConfirm(currentLocations.toList()) }) { 
                    Text("Confirm") 
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel") 
            }
        }
    )
}
