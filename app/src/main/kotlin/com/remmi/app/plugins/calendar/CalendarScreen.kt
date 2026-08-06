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
import kotlinx.coroutines.launch

@Composable
fun CalendarScreen(actions: CalendarActions) {

    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var events by remember { mutableStateOf(emptyList<CalendarItem>()) }

    LaunchedEffect(Unit) {
        events = actions.getAllEvents()
    }

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

            HorizontalDivider()

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

                items(events) { item ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {

                        Text(
                            text = item.title,
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

            onSave = { title, description, day, month, year, startTime, endTime, priority ->

                scope.launch {
                    actions.addEvent(title, description, day, month, year, startTime, endTime, priority)
                    events = actions.getAllEvents()
                    showAddDialog = false
                }
            }

        )

    }
}


@Composable
fun AddEventDialog( onDismiss: () -> Unit,
                    onSave: (
                        title: String,
                        description: String,
                        day: String,
                        month: String,
                        year: String,
                        startTime: String,
                        endTime: String,
                        priority: Priority
                    ) -> Unit

) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
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
                Row {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = day,
                        onValueChange = { day = it },
                        label = { Text("Day") }
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = month,
                        onValueChange = { month = it },
                        label = { Text("Month") }
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Year") }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = startTime,
                        onValueChange = { startTime = it
                        },
                        label = { Text("Start (HH:mm)")
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = endTime,
                        onValueChange = { endTime = it
                        },
                        label = { Text("End (HH:mm)")
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
                        day,
                        month,
                        year,
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
