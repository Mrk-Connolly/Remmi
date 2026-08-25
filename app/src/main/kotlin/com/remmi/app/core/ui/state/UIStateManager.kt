package com.remmi.app.core.ui.state

import androidx.compose.runtime.mutableStateOf

/**
 * UI STATE MANAGER
 * 
 * Centralized holder for global UI flags and session-specific states.
 */
class UIStateManager {
    
    /** Indicates if a full-screen editor is currently active (hides menus) */
    val isEditorActive = mutableStateOf(false)
    
    /** Visibility of the main island navigation menu */
    val isMenuVisible = mutableStateOf(true)

    /** Location Picker State */
    val showLocationPicker = mutableStateOf(false)
    val locationPickerRequestId = mutableStateOf("")
    val locationPickerInitialSearch = mutableStateOf<String?>(null)

    init {
        // Initialization if needed
    }
}
