package com.remmi.app.plugins.transcriptions.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.ui.components.RemmiSecondaryScreen
import com.remmi.app.plugins.transcriptions.TranscriptionsActions
import com.remmi.app.plugins.transcriptions.models.TranscriptionItem
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptionDetailScreen(
    item: TranscriptionItem,
    actions: TranscriptionsActions,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // Reactive observation of this specific item
    val transcriptions by actions.getTranscriptions().collectAsState(initial = emptyList())
    val currentItem = transcriptions.find { it.id == item.id } ?: item

    RemmiSecondaryScreen(
        title = "", // Empty title as per user request to remove top title
        onBack = onBack,
        topBarActions = {
            IconButton(onClick = { /* Edit */ }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { 
                scope.launch { 
                    actions.deleteTranscription(currentItem.id)
                    onBack()
                }
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            val date = currentItem.created.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val duration = formatDuration(currentItem.durationMillis)
            
            Text(text = "$date · $duration", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            if (!currentItem.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(text = currentItem.description, style = MaterialTheme.typography.bodyMedium)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            if (currentItem.status == "RECORDING") {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        text = "🔴 RECORDING LIVE",
                        color = androidx.compose.ui.graphics.Color.Red,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "(Optimized for Lecture)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { 
                    currentItem.audioFilePath?.let { scope.launch { actions.playAudio(it) } }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = currentItem.audioFilePath != null
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Play Recording")
            }

            OutlinedButton(
                onClick = { 
                    currentItem.textFilePath?.let { scope.launch { actions.openFile(it) } }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                enabled = currentItem.textFilePath != null
            ) {
                Icon(Icons.Default.FileOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open TXT File")
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "PREVIEW",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = currentItem.transcript ?: "No transcript text found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
