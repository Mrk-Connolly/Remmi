package com.remmi.app.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * STARTUP PERMISSION CHECK
 * 
 * Verifies that the app has necessary permissions on startup.
 * Displays an explanation dialog if permissions are missing.
 */
@Composable
fun StartupPermissionCheck() {
    val context = LocalContext.current
    
    val requiredPermissions = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_CONTACTS
    )
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    var missingPermissions by remember {
        mutableStateOf(requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        })
    }

    var showDialog by remember { mutableStateOf(missingPermissions.isNotEmpty()) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        // Check what's still missing after the request
        missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        showDialog = false
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = { Icon(Icons.Default.Security, contentDescription = null) },
            title = { Text("Permissions Required") },
            text = {
                Text(
                    "To provide call recording, smart transcriptions, and assistant notifications, " +
                    "Remmi needs access to your microphone, phone status, and contacts. " +
                    "Please grant these permissions to continue."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        launcher.launch(requiredPermissions.toTypedArray())
                    }
                ) {
                    Text("Grant Permissions")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Later")
                }
            }
        )
    }
}
