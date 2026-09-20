package com.remmi.app.core.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * GLOBAL UI STATE
 *
 * Centralized holder for truly global UI flags and pending cross-plugin requests.
 */
object GlobalUIState {
    
    /** Indicates if a full-screen editor or secondary screen is currently active (hides bottom menu) */
    var isEditorActive by mutableStateOf(false)
    
    /** Visibility of the main island navigation menu */
    var isMenuVisible by mutableStateOf(true)

    /** Location Picker State */
    var showLocationPicker by mutableStateOf(false)
    var locationPickerData by mutableStateOf<LinkedCreationData?>(null)

    /** Linked Item Creation Popups */
    var pendingAlarmRequest by mutableStateOf<LinkedCreationData?>(null)
    var pendingTaskRequest by mutableStateOf<LinkedCreationData?>(null)
    var pendingContactRequest by mutableStateOf<LinkedCreationData?>(null)
    
    /** Tracking for successful completion of linked requests */
    var lastConfirmedCorrelationId by mutableStateOf<String?>(null)

    /** Receipt Scan State */
    var pendingReceiptImageRequest by mutableStateOf<ReceiptImageData?>(null)

    /** Appearance State */
    var themePreference by mutableStateOf(RemmiThemeMode.LIGHT)
    var primaryColorHex by mutableStateOf("#6200EE") // Default Purple
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
