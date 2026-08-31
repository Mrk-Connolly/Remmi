package com.remmi.app.core.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp

/**
 * REMMI EDITOR SCAFFOLD
 * Standardized layout for plugin editor screens, including navigation and save actions
 */
@Composable
fun RemmiEditorScaffold(
    title: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {

    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    val keyboardController = LocalSoftwareKeyboardController.current

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
                        shape = androidx.compose.foundation.shape.CircleShape,
                        onClick = {
                            keyboardController?.hide()
                            onBack()
                        }
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        onClick = {
                            keyboardController?.hide()
                            onSave()
                        },
                        enabled = saveEnabled
                    ) {
                        Text("Save")
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
