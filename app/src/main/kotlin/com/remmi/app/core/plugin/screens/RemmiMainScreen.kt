package com.remmi.app.core.plugin.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.remmi.app.core.controller.GlobalUIState

/**
 * REMMI MAIN SCREEN
 * The primary entry point for a plugin.
 * Features a visible bottom navigation menu and optional top bar actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemmiMainScreen(
    title: String,
    topBarActions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    // Ensure bottom menu is visible
    DisposableEffect(Unit) {
        GlobalUIState.isEditorActive.value = false
        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = topBarActions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = floatingActionButton,
        content = content
    )
}
