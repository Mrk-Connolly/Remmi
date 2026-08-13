package com.remmi.app.plugins.calendar

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek

@Composable
fun RepeatDaysDialog(
    selectedDays: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    Log.d("Remmi", "[RepeatDaysDialog] - [RepeatDaysDialog] executed")
    val days = DayOfWeek.entries
    val currentSelected = remember { mutableStateListOf<String>().apply { addAll(selectedDays) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Days") },
        text = {
            Column {
                days.forEach { day ->
                    val dayStr = day.name
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                        if (currentSelected.contains(dayStr)) currentSelected.remove(dayStr) else currentSelected.add(dayStr)
                    }) {
                        Checkbox(checked = currentSelected.contains(dayStr), onCheckedChange = {
                            if (it) currentSelected.add(dayStr) else currentSelected.remove(dayStr)
                        })
                        Text(day.name.lowercase().replaceFirstChar { it.uppercase() })
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
