package com.remmi.app.plugins.ingredients.popups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.remmi.app.plugins.ingredients.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScanResultsPopup(
    results: List<ReceiptItemMatch>,
    onDismiss: () -> Unit,
    onConfirm: (List<ReceiptItemMatch>) -> Unit
) {
    var editableResults by remember { mutableStateOf(results) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Receipt Scan Results") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        TextButton(onClick = { onConfirm(editableResults) }) {
                            Text("Import")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                if (editableResults.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No items detected.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(editableResults) { index, match ->
                            ReceiptMatchRow(
                                match = match,
                                onUpdate = { updated ->
                                    val newList = editableResults.toMutableList()
                                    newList[index] = updated
                                    editableResults = newList
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptMatchRow(
    match: ReceiptItemMatch,
    onUpdate: (ReceiptItemMatch) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (match.status) {
                MatchStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                MatchStatus.IGNORED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.receiptItem.detectedName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Original: ${match.receiptItem.originalText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                match.receiptItem.price?.let {
                    Text(
                        text = "$${"%.2f".format(it)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (match.confidence) {
                    MatchConfidence.HIGH -> Icons.Default.Check
                    MatchConfidence.MEDIUM -> Icons.Default.Warning
                    else -> Icons.Default.Warning
                }
                val iconColor = when (match.confidence) {
                    MatchConfidence.HIGH -> Color(0xFF4CAF50)
                    MatchConfidence.MEDIUM -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }

                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                
                Text(
                    text = if (match.matchedIngredient != null) "Matched: ${match.matchedIngredient.name}" else "Unmatched",
                    style = MaterialTheme.typography.bodyMedium,
                    color = iconColor
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (match.status != MatchStatus.CONFIRMED) {
                    Button(
                        onClick = { onUpdate(match.copy(status = MatchStatus.CONFIRMED)) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Confirm", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onUpdate(match.copy(status = MatchStatus.PENDING)) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Undo", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (match.status != MatchStatus.IGNORED) {
                    TextButton(
                        onClick = { onUpdate(match.copy(status = MatchStatus.IGNORED)) },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Ignore", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
