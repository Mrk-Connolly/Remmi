package com.remmi.app.core.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun RemmiColorPicker(
    initialColor: String,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    var selectedColor by remember { mutableStateOf(initialColor) }
    var customHex by remember { mutableStateOf(initialColor) }
    var isGradient by remember { mutableStateOf(false) }
    
    val presets = listOf(
        "#7F3DFF", "#0077FF", "#00B159", "#FF9F00", "#FF4081", "#00BFA5",
        "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            modifier = Modifier.width(320.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text("Select Color", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                // Color Preview
                val colorObj = remember(selectedColor) {
                    try { Color(android.graphics.Color.parseColor(selectedColor)) } catch (e: Exception) { Color.Gray }
                }
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGradient) {
                                Brush.linearGradient(
                                    colors = listOf(colorObj, colorObj.copy(alpha = 0.6f))
                                )
                            } else {
                                Brush.linearGradient(colors = listOf(colorObj, colorObj))
                            }
                        )
                )

                // Custom Hex Input
                OutlinedTextField(
                    value = customHex,
                    onValueChange = { 
                        customHex = it
                        if (it.startsWith("#") && (it.length == 7 || it.length == 9)) {
                            try {
                                android.graphics.Color.parseColor(it)
                                selectedColor = it
                            } catch (e: Exception) {}
                        }
                    },
                    label = { Text("Hex Color") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Gradient Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Gradient", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isGradient, onCheckedChange = { isGradient = it })
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    presets.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                                .border(
                                    width = if (selectedColor == hex) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { 
                                    selectedColor = hex
                                    customHex = hex
                                }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onColorSelected(selectedColor); onDismiss() }) { Text("Select") }
                }
            }
        }
    }
}
