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

    RemmiSecondaryScreen(
        title = "", // Empty title as per user request to remove top title
        onBack = onBack,
        topBarActions = {
            IconButton(onClick = { /* Edit */ }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { 
                scope.launch { 
                    actions.deleteTranscription(item.id)
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
            val date = item.created.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val duration = formatDuration(item.durationMillis)
            
            Text(text = "$date · $duration", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            if (!item.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(text = "TRANSCRIPT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = item.transcript ?: "No transcript available.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { /* Play recording */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Play Recording")
            }

            OutlinedButton(
                onClick = { /* Export TXT */ },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Default.FileOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open TXT File")
            }
        }
    }
}
