package com.remmi.app.plugins.callrecorder.ui

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.remmi.app.plugins.callrecorder.*
import com.remmi.app.plugins.callrecorder.models.CallDirection
import com.remmi.app.plugins.callrecorder.models.CallRecording
import com.remmi.app.plugins.callrecorder.models.RecordingStatus
import com.remmi.app.ui.DesignTokens
import com.remmi.app.ui.components.RemmiCard
import com.remmi.app.ui.components.RemmiHomeScreen
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallRecorderScreen(
    actions: CallRecorderActions
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val recordings by actions.recordings.collectAsState()
    val viewMode by actions.viewMode.collectAsState()

    // Permissions for call recording
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            Log.w("Remmi", "[CallRecorderScreen] - Some permissions were denied")
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    RemmiHomeScreen(
        title = "Call Recorder"
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            RemmiCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Call Recordings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${recordings.size} recordings saved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // View Mode Toggle
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = viewMode == CallRecorderViewMode.CONTACTS,
                    onClick = { actions.setViewMode(CallRecorderViewMode.CONTACTS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(Icons.Default.Person, null) }
                ) {
                    Text("Contacts")
                }
                SegmentedButton(
                    selected = viewMode == CallRecorderViewMode.GROUPS,
                    onClick = { actions.setViewMode(CallRecorderViewMode.GROUPS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Default.Folder, null) }
                ) {
                    Text("Groups")
                }
            }

            if (recordings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No recordings yet.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else {
                val groupedRecordings = remember(recordings, viewMode) {
                    if (viewMode == CallRecorderViewMode.CONTACTS) {
                        recordings.groupBy { it.contactName ?: it.phoneNumber ?: "Unknown" }
                    } else {
                        recordings.groupBy { it.group ?: "Unassigned" }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    groupedRecordings.forEach { (header, items) ->
                        item {
                            ContactHeaderRow(
                                title = header,
                                count = items.size,
                                isContact = viewMode == CallRecorderViewMode.CONTACTS
                            )
                        }
                        items(items, key = { it.id }) { recording ->
                            RecordingRow(recording, actions)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactHeaderRow(title: String, count: Int, isContact: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isContact) Icons.Default.AccountCircle else Icons.Default.FolderOpen,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape
        ) {
            Text(
                text = "$count",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun RecordingRow(recording: CallRecording, actions: CallRecorderActions) {
    val scope = rememberCoroutineScope()
    val dateTime = recording.created.toLocalDateTime(TimeZone.currentSystemDefault())
    val dateStr = "${dateTime.day}/${dateTime.monthNumber}/${dateTime.year}"
    val timeStr = "${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
    
    val isPlaying by actions.isPlaying.collectAsState()
    val currentPath by actions.currentPlayingPath.collectAsState()
    val isThisPlaying = isPlaying && currentPath == recording.filePath
    val context = LocalContext.current

    RemmiCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (recording.direction == CallDirection.INCOMING) 
                            MaterialTheme.colorScheme.secondaryContainer 
                        else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (recording.direction == CallDirection.INCOMING) 
                        Icons.AutoMirrored.Filled.CallReceived 
                    else Icons.AutoMirrored.Filled.CallMade,
                    contentDescription = null,
                    tint = if (recording.direction == CallDirection.INCOMING) 
                        MaterialTheme.colorScheme.onSecondaryContainer 
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recording.contactName ?: recording.phoneNumber ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$dateStr • $timeStr • ${formatDuration(recording.durationMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (recording.group != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = recording.group.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                if (recording.status == RecordingStatus.FAILED) {
                    Text(
                        text = "Failed: ${recording.errorMessage}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            IconButton(onClick = { actions.playRecording(recording) }) {
                Icon(
                    imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isThisPlaying) "Pause" else "Play"
                )
            }
            
            IconButton(onClick = { actions.shareRecording(context, recording) }) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }

            var showGroupMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showGroupMenu = true }) {
                    Icon(Icons.Default.Folder, contentDescription = "Add to Group")
                }
                DropdownMenu(
                    expanded = showGroupMenu,
                    onDismissRequest = { showGroupMenu = false }
                ) {
                    val groups = listOf("Work", "Family", "Friends", "Important")
                    groups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group) },
                            onClick = {
                                scope.launch {
                                    actions.addToGroup(recording, group)
                                    showGroupMenu = false
                                }
                            }
                        )
                    }
                    if (recording.group != null) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Remove Group") },
                            onClick = {
                            scope.launch {
                                actions.addToGroup(recording, null)
                                showGroupMenu = false
                            }
                        }
                        )
                    }
                }
            }

            IconButton(onClick = {
                scope.launch {
                    actions.deleteRecording(recording)
                }
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
