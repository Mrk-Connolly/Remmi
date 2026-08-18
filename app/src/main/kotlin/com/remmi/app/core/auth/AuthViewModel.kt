package com.remmi.app.core.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * AUTH VIEW MODEL
 *
 * Manages UI state and business logic for the Authentication screens.
 */
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    var isSignUpMode by mutableStateOf(false)


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Perform Auth
     * Executes sign-in or sign-up based on the current mode.
     */
    fun performAuth(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password cannot be empty"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                if (isSignUpMode) {
                    repository.signUp(email, password)
                } else {
                    repository.signIn(email, password)
                }
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Authentication failed"
            } finally {
                isLoading = false
            }
        }
    }

    /**                                 Switch Mode
     * Toggles between Sign In and Sign Up screens.
     */
    fun toggleMode() {
        isSignUpMode = !isSignUpMode
        errorMessage = null
    }
}
