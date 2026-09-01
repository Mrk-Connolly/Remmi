package com.remmi.app.plugins.ingredients.ui.popups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.remmi.app.ui.components.RemmiCard
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
    val cardColor = when (match.status) {
        MatchStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        MatchStatus.IGNORED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    RemmiCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = cardColor
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.receiptItem.detectedName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Scan: ${match.receiptItem.originalText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                match.receiptItem.price?.let {
                    Text(
                        text = "$${"%.2f".format(it)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (match.confidence) {
                    MatchConfidence.HIGH -> Icons.Default.CheckCircle
                    MatchConfidence.MEDIUM -> Icons.Default.Warning
                    else -> Icons.AutoMirrored.Filled.Help
                }
                val iconColor = when (match.confidence) {
                    MatchConfidence.HIGH -> Color(0xFF4CAF50)
                    MatchConfidence.MEDIUM -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.primary
                }

                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))

                Text(
                    text = if (match.matchedIngredient != null) "Matched to ${match.matchedIngredient.name}" else "No match found",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = iconColor
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (match.status != MatchStatus.CONFIRMED) {
                    Button(
                        onClick = { onUpdate(match.copy(status = MatchStatus.CONFIRMED)) },
                        shape = CircleShape,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Confirm", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onUpdate(match.copy(status = MatchStatus.PENDING)) },
                        shape = CircleShape,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Undo", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (match.status != MatchStatus.IGNORED) {
                    TextButton(
                        onClick = { onUpdate(match.copy(status = MatchStatus.IGNORED)) },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            "Ignore",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
