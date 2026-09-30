package com.remmi.app.core.android.launcher

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * Service to manage Default Launcher state and trigger Android System Home Settings.
 */
class LauncherManagerService(private val context: Context) {

    /**
     * Check whether Remmi is currently selected as the system default Home/Launcher app.
     */
    fun isDefaultLauncher(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                roleManager?.isRoleHeld(RoleManager.ROLE_HOME) ?: false
            } else {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                }
                val resolveInfo = context.packageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                resolveInfo?.activityInfo?.packageName == context.packageName
            }
        } catch (e: Exception) {
            Log.e("Remmi", "[LauncherManagerService] - Error checking default launcher", e)
            false
        }
    }

    /**
     * Create an intent to prompt the user to select Remmi as the Default Home app.
     */
    fun createSetDefaultLauncherIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            } else {
                Intent(Settings.ACTION_HOME_SETTINGS)
            }
        } else {
            Intent(Settings.ACTION_HOME_SETTINGS)
        }
    }

    /**
     * Open system home settings chooser.
     */
    fun openHomeSettings() {
        try {
            val intent = createSetDefaultLauncherIntent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("Remmi", "[LauncherManagerService] - Error opening home settings", e)
            // Fallback to general settings
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Log.e("Remmi", "[LauncherManagerService] - Fallback settings failed", ex)
            }
        }
    }

    /**
     * Expand system notifications panel (for swipe down gesture).
     */
    fun openNotificationShade() {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(statusBarService)
        } catch (e: Exception) {
            Log.w("Remmi", "[LauncherManagerService] - Unable to expand notifications panel via reflection", e)
        }
    }
}
