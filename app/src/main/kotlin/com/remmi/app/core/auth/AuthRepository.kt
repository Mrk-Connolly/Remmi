package com.remmi.app.core.auth

import android.util.Log
import com.remmi.app.core.service.database.SupabaseService
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * AUTH REPOSITORY
 *
 * Manages Supabase Authentication sessions and user operations.
 */
class AuthRepository {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    private val auth = SupabaseService.client.auth

    /** Current authentication status as a flow */
    val sessionStatus: Flow<AuthState> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> AuthState.Authenticated
            is SessionStatus.Initializing -> AuthState.Loading
            is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
            is SessionStatus.RefreshFailure -> AuthState.Unauthenticated
        }
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Sign Up
     * Create a new account with email and password.
     */
    suspend fun signUp(email: String, pass: String) {
        Log.d("Remmi", "[AuthRepository] - Sign up attempt: $email")
        auth.signUpWith(Email) {
            this.email = email
            this.password = pass
        }
    }

    /**                                 Sign In
     * Authenticate with existing credentials.
     */
    suspend fun signIn(email: String, pass: String) {
        Log.d("Remmi", "[AuthRepository] - Sign in attempt: $email")
        auth.signInWith(Email) {
            this.email = email
            this.password = pass
        }
    }

    /**                                 Sign Out
     * Terminate the current session.
     */
    suspend fun signOut() {
        Log.d("Remmi", "[AuthRepository] - Signing out")
        auth.signOut()
    }

    /**                                 Get User
     * Retrieve the currently authenticated user information.
     */
    fun getCurrentUser(): UserInfo? {
        return auth.currentUserOrNull()
    }

    /**                                 Get User ID
     * Retrieve the ID of the currently authenticated user.
     */
    fun getCurrentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }
}

/**
 * AUTH STATE
 * Represents the high-level authentication status of the application.
 */
enum class AuthState {
    Loading,
    Authenticated,
    Unauthenticated
}
