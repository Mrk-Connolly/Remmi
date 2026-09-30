package com.remmi.app.core.android.google

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.remmi.app.core.android.services.SystemSettingsService
import com.remmi.app.core.controller.RemmiComponent

/**
 * GOOGLE AUTH SERVICE
 *
 * Manages Google Sign-In and authorization for Drive access.
 */
class GoogleAuthService(
    private val context: Context,
    private val settingsService: SystemSettingsService
) : RemmiComponent {

    companion object {
        private const val TAG = "GoogleAuthService"
    }

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
        .build()

    private val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    override suspend fun start() {
        Log.d(TAG, "[GoogleAuthService] - Started")
    }

    override fun stop() {
        Log.d(TAG, "[GoogleAuthService] - Stopped")
    }

    /**
     * Returns the intent to start the Google Sign-In flow.
     */
    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    /**
     * Returns the currently signed-in account, if any.
     */
    fun getLastSignedInAccount(): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

    /**
     * Signs out the current account.
     */
    fun signOut(onComplete: () -> Unit) {
        Log.d(TAG, "[signOut] - Signing out")
        googleSignInClient.signOut().addOnCompleteListener {
            Log.i(TAG, "[signOut] - Success")
            onComplete()
        }
    }
}
