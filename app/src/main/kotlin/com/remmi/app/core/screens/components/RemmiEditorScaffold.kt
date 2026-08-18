package com.remmi.app.core.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

    Scaffold(
        bottomBar = {
            Column {
                Surface(tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onBack
                        ) {
                            Text("Back")
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onSave,
                            enabled = saveEnabled
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
            content()
        }
    }
}
