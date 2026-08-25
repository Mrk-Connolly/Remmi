package com.remmi.app.core.Users

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.remmi.app.core.auth.AuthRepository
import kotlin.time.Instant
import kotlin.time.Clock

/**
 * USER VIEW MODEL
 *
 * Manages the application user profile, creating it on first sign up
 * and keeping it synchronized with the authenticated Supabase user.
 */
class UserViewModel(
    private val repository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Current user profile, null until the profile is created or loaded */
    var user by mutableStateOf<User?>(null)
        private set

    /** Guard against concurrent profile operations */
    private var inProgress = false


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Ensure Profile
     * Creates the user profile row on a new sign-up, or loads it on sign-in.
     * Skips the existence check on sign-up (the account is brand new) to save a
     * round trip, and runs fully off the main thread.
     */
    suspend fun ensureProfile(name: String = "", isNewUser: Boolean = false) {
        if (inProgress) return
        inProgress = true
        try {
            Log.d("Remmi", "[UserViewModel] - ensureProfile executed")

            val authUser = authRepository.getCurrentUser() ?: return

            val profile = if (isNewUser) {
                createProfile(authUser.id, authUser.email, name)
            } else {
                // Signed-in account: fetch the existing row, or recreate it if missing.
                repository.getById(authUser.id) ?: createProfile(authUser.id, authUser.email, name)
            }

            user = profile
        } finally {
            inProgress = false
        }
    }

    private suspend fun createProfile(id: String, email: String?, name: String): User {
        val now: Instant = Clock.System.now()
        val profile = User(
            id = id,
            created = now,
            modified = now,
            name = name,
            email = email ?: "",
            userId = id
        )
        repository.insert(profile)
        Log.d("Remmi", "[UserViewModel] - Profile created for user: $id")
        return profile
    }
}