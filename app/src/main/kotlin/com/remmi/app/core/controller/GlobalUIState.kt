package com.remmi.app.core.controller

import androidx.compose.runtime.mutableStateOf

/**
 * GLOBAL UI STATE
 *
 * Centralized holder for truly global UI flags and pending cross-plugin requests.
 */
object GlobalUIState {
    
    /** Indicates if a full-screen editor or secondary screen is currently active (hides bottom menu) */
    val isEditorActive = mutableStateOf(false)
    
    /** Visibility of the main island navigation menu */
    val isMenuVisible = mutableStateOf(true)

    /** Location Picker State */
    val showLocationPicker = mutableStateOf(false)
    val locationPickerData = mutableStateOf<LinkedCreationData?>(null)

    /** Linked Item Creation Popups */
    val pendingAlarmRequest = mutableStateOf<LinkedCreationData?>(null)
    val pendingTaskRequest = mutableStateOf<LinkedCreationData?>(null)
    val pendingContactRequest = mutableStateOf<LinkedCreationData?>(null)
    
    /** Tracking for successful completion of linked requests */
    val lastConfirmedCorrelationId = mutableStateOf<String?>(null)

    /** Receipt Scan State */
    val pendingReceiptImageRequest = mutableStateOf<ReceiptImageData?>(null)

    /** Appearance State */
    val themePreference = mutableStateOf(RemmiThemeMode.SYSTEM)
    val primaryColorHex = mutableStateOf("#6200EE") // Default Purple
}

enum class RemmiThemeMode {
    LIGHT, DARK, SYSTEM
}

/**
 * Data required for receipt image request.
 */
data class ReceiptImageData(
    val requestId: String,
    val useCamera: Boolean
)

/**
 * Data required to pre-fill a linked item creation popup.
 */
data class LinkedCreationData(
    val title: String,
    val description: String,
    val sourcePlugin: String,
    val sourceItemId: String,
    val correlationId: String?,
    val causationId: String?
)
