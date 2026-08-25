package com.remmi.app.core.auth

import kotlinx.coroutines.flow.Flow

/**
 * AUTH REPOSITORY CONTRACT
 *
 * Provides access to the currently authenticated user and the auth session
 * status. Implementations are expected to back this with the active auth
 * provider (e.g. Supabase).
 */
interface AuthRepository {

    /** Emits the current authentication status as it changes. */
    val sessionStatus: Flow<AuthState>

    /** Returns the currently authenticated user, or null if not signed in. */
    suspend fun getCurrentUser(): AuthUser?

    /** Signs in with email and password. Throws on failure. */
    suspend fun signIn(email: String, password: String)

    /** Creates a new account with email and password. Throws on failure. */
    suspend fun signUp(email: String, password: String)

    /** Clears the current session. */
    suspend fun signOut()
}

/**
 * Lightweight representation of an authenticated user.
 */
data class AuthUser(
    val id: String,
    val email: String?
)

/**
 * Authentication status states consumed by the UI.
 */
sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
}
