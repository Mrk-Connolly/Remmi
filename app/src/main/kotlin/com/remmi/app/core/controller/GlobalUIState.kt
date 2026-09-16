package com.remmi.app.core.controller

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * GLOBAL UI STATE
 *
 * Centralized holder for truly global UI flags and pending cross-plugin requests.
 */
object GlobalUIState {
    
    /** Indicates if a full-screen editor or secondary screen is currently active (hides bottom menu) */
    val isEditorActive = MutableStateFlow(false)
    
    /** Visibility of the main island navigation menu */
    val isMenuVisible = MutableStateFlow(true)

    /** Location Picker State */
    val showLocationPicker = MutableStateFlow(false)
    val locationPickerData = MutableStateFlow<LinkedCreationData?>(null)

    /** Linked Item Creation Popups */
    val pendingAlarmRequest = MutableStateFlow<LinkedCreationData?>(null)
    val pendingTaskRequest = MutableStateFlow<LinkedCreationData?>(null)
    val pendingContactRequest = MutableStateFlow<LinkedCreationData?>(null)
    
    /** Tracking for successful completion of linked requests */
    val lastConfirmedCorrelationId = MutableStateFlow<String?>(null)

    /** Receipt Scan State */
    val pendingReceiptImageRequest = MutableStateFlow<ReceiptImageData?>(null)

    /** Appearance State */
    val themePreference = MutableStateFlow(RemmiThemeMode.SYSTEM)
    val primaryColorHex = MutableStateFlow("#6200EE") // Default Purple
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
