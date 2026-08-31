package com.remmi.app.core.plugin.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.remmi.app.core.controller.GlobalUIState

/**
 * REMMI SECONDARY SCREEN
 * Used for descriptions or information panels.
 * Hides the bottom menu and provides a top bar with a back action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemmiSecondaryScreen(
    title: String,
    onBack: () -> Unit,
    topBarActions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
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
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = topBarActions
            )
        },
        floatingActionButton = floatingActionButton,
        content = content
    )
}
