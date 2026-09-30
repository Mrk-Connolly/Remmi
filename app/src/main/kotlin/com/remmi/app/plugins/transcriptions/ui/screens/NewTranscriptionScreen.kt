package com.remmi.app.plugins.transcriptions.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.ui.components.RemmiAddScreen
import com.remmi.app.plugins.transcriptions.TranscriptionsActions
import com.remmi.app.plugins.transcriptions.models.TranscriptionItem
import com.remmi.app.plugins.transcriptions.models.TranscriptionGroup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTranscriptionScreen(
    actions: TranscriptionsActions,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf<TranscriptionGroup?>(null) }
    var selectedLanguage by remember { mutableStateOf("auto") }
    
    var showAddGroup by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }

    var activeTranscriptionId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val transcriptions by actions.getTranscriptions().collectAsState(initial = emptyList())
    val groups by actions.getGroups().collectAsState(initial = emptyList())
    val activeItem = transcriptions.find { it.id == activeTranscriptionId }

    LaunchedEffect(activeItem?.status) {
        if (activeItem?.status == "FINISHED") {
            onDismiss()
        }
    }

    if (activeItem != null && (activeItem.status == "RECORDING" || activeItem.status == "PAUSED" || activeItem.status == "FINISHING")) {
        RecordingScreen(
            item = activeItem,
            actions = actions,
            onFinish = { onDismiss() }
        )
    } else {
        RemmiAddScreen(
            title = "New Transcription",
            onBack = onDismiss,
            onSave = {
                val id = UUID.randomUUID().toString()
                activeTranscriptionId = id
                scope.launch {
                    actions.startRecording(id, title, description, selectedGroup?.id, selectedLanguage)
                }
            },
            saveEnabled = title.isNotBlank()
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Group Selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Group", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = selectedGroup?.name ?: "None Selected",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showAddGroup = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Group")
                }
            }
            
            // Language Selection
            Text("Language", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedLanguage == "auto",
                    onClick = { selectedLanguage = "auto" },
                    label = { Text("Auto") }
                )
                FilterChip(
                    selected = selectedLanguage == "en",
                    onClick = { selectedLanguage = "en" },
                    label = { Text("English") }
                )
                FilterChip(
                    selected = selectedLanguage == "es",
                    onClick = { selectedLanguage = "es" },
                    label = { Text("Spanish") }
                )
            }

            if (showAddGroup) {
                AlertDialog(
                    onDismissRequest = { showAddGroup = false },
                    title = { Text("New Group") },
                    text = {
                        OutlinedTextField(
                            value = newGroupName,
                            onValueChange = { newGroupName = it },
                            label = { Text("Group Name") }
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newGroupName.isNotBlank()) {
                                    scope.launch {
                                        actions.createGroup(newGroupName)
                                        newGroupName = ""
                                        showAddGroup = false
                                    }
                                }
                            }
                        ) { Text("Create") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddGroup = false }) { Text("Cancel") }
                    }
                )
            }

            // Group selector dropdown/list (simplified for now)
            if (groups.isNotEmpty()) {
                Text("Select Group", style = MaterialTheme.typography.labelSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    groups.forEach { group ->
                        InputChip(
                            selected = selectedGroup?.id == group.id,
                            onClick = { selectedGroup = group },
                            label = { Text(group.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecordingScreen(
    item: TranscriptionItem,
    actions: TranscriptionsActions,
    onFinish: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var timer by remember { mutableStateOf(0L) }
    
    LaunchedEffect(item.status) {
        if (item.status == "RECORDING") {
            while (true) {
                delay(1000)
                timer += 1000
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        Spacer(Modifier.height(32.dp))

        if (item.status == "RECORDING") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔴 RECORDING", color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(text = "• Live Capture Active", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (item.status == "PAUSED") {
            Text(text = "⏸ PAUSED", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }

        Text(
            text = formatDuration(timer),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            fontSize = 64.sp
        )

        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Transcribing...", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.transcript ?: "Wait for speech...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (item.status == "RECORDING") {
                Button(onClick = { scope.launch { actions.pauseRecording(item.id) } }) {
                    Icon(Icons.Default.Pause, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pause")
                }
            } else if (item.status == "PAUSED") {
                Button(onClick = { scope.launch { actions.resumeRecording(item.id) } }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Resume")
                }
            }

            Button(
                onClick = { 
                    scope.launch { 
                        actions.finishRecording(item.id)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Finish")
            }
        }
    }
}
