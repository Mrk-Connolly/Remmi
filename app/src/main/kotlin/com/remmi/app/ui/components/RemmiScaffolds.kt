package com.remmi.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.GlobalUIState
import com.remmi.app.ui.DesignTokens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Configuration for Remmi Scaffolds to handle overlay and inset behavior.
 */
data class RemmiScaffoldConfig(
    val useTopOverlay: Boolean = false,
    val topContentPadding: androidx.compose.ui.unit.Dp = 0.dp
)

val LocalRemmiScaffoldConfig = staticCompositionLocalOf { RemmiScaffoldConfig() }

/**
 * REMMI HOME SCREEN SCAFFOLD
 * The primary entry point for a plugin.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemmiHomeScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    topBarActions: (@Composable RowScope.() -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val config = LocalRemmiScaffoldConfig.current

    // Ensure bottom menu is visible
    DisposableEffect(Unit) {
        val previous = GlobalUIState.isEditorActive
        GlobalUIState.isEditorActive = false
        onDispose { 
            GlobalUIState.isEditorActive = previous
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = if (config.useTopOverlay) WindowInsets(0, 0, 0, 0) else WindowInsets.statusBars,
        floatingActionButton = floatingActionButton,
        topBar = {
            if (onBack != null || topBarActions != null) {
                CenterAlignedTopAppBar(
                    title = { /* Title removed as per user request */ },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = topBarActions ?: {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Apply extra padding if we are under a top overlay
                val finalPadding = if (config.useTopOverlay) {
                    PaddingValues(
                        top = config.topContentPadding,
                        bottom = paddingValues.calculateBottomPadding()
                    )
                } else {
                    paddingValues
                }
                content(finalPadding)
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
    content: @Composable (PaddingValues) -> Unit
) {
    DisposableEffect(Unit) {
        val previous = GlobalUIState.isEditorActive
        GlobalUIState.isEditorActive = true // Hide bottom navigation on secondary screens
        onDispose { 
            GlobalUIState.isEditorActive = previous
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = topBarActions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                content(paddingValues)
            }
        }
    )
}

/**
 * REMMI ADD SCREEN SCAFFOLD
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
@OptIn(ExperimentalMaterial3Api::class)
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
        val previous = GlobalUIState.isEditorActive
        GlobalUIState.isEditorActive = true // Hide bottom navigation on editor screens
        onDispose { 
            GlobalUIState.isEditorActive = previous
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            CenterAlignedTopAppBar(
                title = { /* Title removed as per user request */ },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(DesignTokens.SpacingLarge),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium)
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
                .padding(horizontal = DesignTokens.SpacingLarge)
                .padding(top = DesignTokens.SpacingMedium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingLarge)
        ) {
            content()
        }
    }
}
