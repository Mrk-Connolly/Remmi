package com.remmi.app.plugins.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.model.components.Priority
import io.github.boguszpawlowski.composecalendar.SelectableCalendar

@Composable
fun CalendarScreen() {

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDialog = true
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Event"
                )
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            //---------------------------------------------------
            // Monthly calendar
            //---------------------------------------------------

            SelectableCalendar()

            Divider()

            Text(
                text = "Upcoming",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            //---------------------------------------------------
            // Timeline
            //---------------------------------------------------

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {

                items(
                    listOf(
                        "Today - Doctor",
                        "Today - Buy groceries",
                        "Tomorrow - Meeting",
                        "Friday - Dinner"
                    )
                ) { item ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {

                        Text(
                            text = item,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                }

            }

        }

    }

    //---------------------------------------------------
    // Add Event Dialog
    //---------------------------------------------------

    if (showAddDialog) {

        AddEventDialog(
            onDismiss = {
                showAddDialog = false
            },

            onSave = { title, description, date, startTime, endTime, priority -> Unit

                // TODO
                // CalendarAction.addEvent(...)

                showAddDialog = false
            }

        )

    }
}


@Composable
fun AddEventDialog( onDismiss: () -> Unit,
                    onSave: (
                        title: String,
                        description: String,
                        date: String,
                        startTime: String,
                        endTime: String,
                        priority: Priority
                    ) -> Unit

) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.NORMAL) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("New Calendar Event")
        },
        text = {
            Column {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = startTime,
                        onValueChange = { startTime = it
                        },
                        label = { Text("Start")
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = endTime,
                        onValueChange = { endTime = it
                        },
                        label = { Text("End")
                        }
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("Priority")
                Spacer(Modifier.height(4.dp))
                Row {
                    Priority.entries.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = priority == it,
                                onClick = { priority = it
                                }
                            )
                            Text(it.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title,
                        description,
                        date,
                        startTime,
                        endTime,
                        priority
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

