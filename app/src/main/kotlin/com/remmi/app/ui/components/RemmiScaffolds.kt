package com.remmi.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.GlobalUIState
import com.remmi.app.ui.DesignTokens

/**
 * REMMI HOME SCREEN SCAFFOLD
 * The primary entry point for a plugin.
 * Features a visible bottom navigation menu and optional top bar actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemmiHomeScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    topBarActions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    // Ensure bottom menu is visible
    DisposableEffect(Unit) {
        val previous = GlobalUIState.isEditorActive.value
        GlobalUIState.isEditorActive.value = false
        onDispose { 
            GlobalUIState.isEditorActive.value = previous
        }
    }

    Scaffold(
        topBar = {
            if (title.isNotEmpty() || onBack != null) {
                TopAppBar(
                    title = {
                        if (title.isNotEmpty()) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = topBarActions,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        floatingActionButton = floatingActionButton,
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                content(PaddingValues(0.dp))
            }
        }
    )
}

/**
 * REMMI SECONDARY SCREEN SCAFFOLD
 * A simpler screen, typically without the bottom menu.
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
    DisposableEffect(Unit) {
        val previous = GlobalUIState.isEditorActive.value
        GlobalUIState.isEditorActive.value = true
        onDispose { 
            GlobalUIState.isEditorActive.value = previous
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
        floatingActionButton = floatingActionButton
    ) { padding ->
        content(padding)
    }
}

/**
 * REMMI ADD SCREEN SCAFFOLD
 * Standardized layout for creating new items.
 */
@Composable
fun RemmiAddScreen(
    title: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    RemmiEditorBaseScaffold(
        title = title,
        onBack = onBack,
        onSave = onSave,
        saveEnabled = saveEnabled,
        showDelete = false,
        onDelete = {},
        content = content
    )
}

/**
 * REMMI MODIFY SCREEN SCAFFOLD
 * Standardized layout for editing existing items.
 */
@Composable
fun RemmiModifyScreen(
    title: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    saveEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    RemmiEditorBaseScaffold(
        title = title,
        onBack = onBack,
        onSave = onSave,
        saveEnabled = saveEnabled,
        showDelete = true,
        onDelete = onDelete,
        content = content
    )
}

/**
 * Internal Base Scaffold for Editor screens (Add/Modify)
 */
@Composable
private fun RemmiEditorBaseScaffold(
    title: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean,
    showDelete: Boolean,
    onDelete: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    DisposableEffect(Unit) {
        val previous = GlobalUIState.isEditorActive.value
        GlobalUIState.isEditorActive.value = true
        onDispose { 
            GlobalUIState.isEditorActive.value = previous
        }
    }

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RemmiSecondaryButton(
                            text = "Back",
                            onClick = {
                                keyboardController?.hide()
                                onBack()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        RemmiButton(
                            text = "Save",
                            onClick = {
                                keyboardController?.hide()
                                onSave()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = saveEnabled
                        )
                    }
                    
                    if (showDelete) {
                        RemmiDeleteButton(
                            text = "Delete",
                            onClick = {
                                keyboardController?.hide()
                                onDelete()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge
            )
            content()
        }
    }
}
