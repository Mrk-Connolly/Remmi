package com.remmi.app.core.plugin.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.GlobalUIState

/**
 * REMMI ADD SCREEN
 * Used to create new items.
 * Features a bottom bar with Back and Save actions. No top menu.
 */
@Composable
fun RemmiAddScreen(
    title: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    // Hide bottom menu
    DisposableEffect(Unit) {
        GlobalUIState.isEditorActive.value = true
        onDispose {
            GlobalUIState.isEditorActive.value = false
        }
    }

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = CircleShape,
                        onClick = onBack
                    ) {
                        Text("Back")
                    }
                    Button(
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = CircleShape,
                        onClick = onSave,
                        enabled = saveEnabled
                    ) {
                        Text("Save")
                    }
                }
            }
        },
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                // Large Title at top of content since there is no TopAppBar
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(24.dp)
                )
                content(PaddingValues(0.dp))
            }
        }
    )
}
