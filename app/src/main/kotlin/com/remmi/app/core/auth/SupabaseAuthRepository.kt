package com.remmi.app.core.auth

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.providers.builtin.Email
import com.remmi.app.core.service.database.SupabaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull

/**
 * SUPABASE AUTH REPOSITORY
 *
 * Thin wrapper over the Supabase Auth plugin. The authenticated user id is
 * obtained from the live Supabase session, which is already maintained by
 * [SupabaseService].
 */
class SupabaseAuthRepository : AuthRepository {

    private val auth = SupabaseService.client.auth

    override val sessionStatus: Flow<AuthState> = flow {
        // Supabase starts in SessionStatus.Initializing and only leaves it once the
        // session is loaded from storage (or a stored session is refreshed). If that
        // never completes (no network, invalid anon key, unrefreshable session) the
        // app would be stuck on the loading spinner forever and never reach the login
        // screen. Bound the wait: fall back to Unauthenticated after a short timeout
        // so the user can always attempt to sign in. If Supabase recovers later, the
        // trailing collect still transitions to the correct state.
        val resolved = withTimeoutOrNull(3_000) {
            auth.sessionStatus.first { it !is SessionStatus.Initializing }
        }
        val start = resolved ?: SessionStatus.NotAuthenticated()
        emit(mapStatus(start))
        auth.sessionStatus.drop(1).collect { emit(mapStatus(it)) }
    }

    private fun mapStatus(status: SessionStatus): AuthState = when (status) {
        is SessionStatus.Initializing -> AuthState.Loading
        // A refresh failure (e.g. expired/invalid token, no network) means the
        // user is effectively signed out. Treat it as unauthenticated so the
        // login screen is reachable instead of an endless loading spinner.
        is SessionStatus.RefreshFailure -> AuthState.Unauthenticated
        is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
        is SessionStatus.Authenticated -> {
            val user = status.session.user
            if (user != null) {
                AuthState.Authenticated(AuthUser(user.id, user.email))
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    override suspend fun getCurrentUser(): AuthUser? {
        val user = auth.currentUserOrNull() ?: return null
        return AuthUser(user.id, user.email)
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signUp(email: String, password: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
