package com.remmi.app.core.android.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * App model representing an installed application.
 */
data class AppModel(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable? = null,
    val launchCount: Int = 0
)

/**
 * Service responsible for discovering installed applications, searching, launching,
 * and tracking launch counts.
 */
class LauncherAppService(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val prefs = context.getSharedPreferences("remmi_launcher_app_usage", Context.MODE_PRIVATE)

    /**
     * Fetch list of all installed launcher applications asynchronously.
     */
    suspend fun getInstalledApps(): List<AppModel> = withContext(Dispatchers.IO) {
        try {
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            
            val resolveInfos: List<ResolveInfo> = packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_ALL
            )

            val currentPackageName = context.packageName

            val apps = resolveInfos.mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                // Optionally exclude self if desired, but keeping self allows opening Remmi app views
                if (pkgName == currentPackageName) return@mapNotNull null

                val label = resolveInfo.loadLabel(packageManager).toString()
                val activityName = resolveInfo.activityInfo.name
                val icon = try {
                    resolveInfo.loadIcon(packageManager)
                } catch (e: Exception) {
                    null
                }
                val launchCount = prefs.getInt("launch_count_$pkgName", 0)

                AppModel(
                    label = label,
                    packageName = pkgName,
                    activityName = activityName,
                    icon = icon,
                    launchCount = launchCount
                )
            }.sortedBy { it.label.lowercase() }

            apps
        } catch (e: Exception) {
            Log.e("Remmi", "[LauncherAppService] - Error fetching installed apps", e)
            emptyList()
        }
    }

    /**
     * Filter installed apps by query text.
     */
    fun filterApps(apps: List<AppModel>, query: String): List<AppModel> {
        if (query.isBlank()) return apps
        val trimmed = query.trim().lowercase()
        return apps.filter { it.label.lowercase().contains(trimmed) }
    }

    /**
     * Get top frequently launched apps.
     */
    fun getFrequentlyUsedApps(apps: List<AppModel>, limit: Int = 5): List<AppModel> {
        return apps.filter { it.launchCount > 0 }
            .sortedByDescending { it.launchCount }
            .take(limit)
    }

    /**
     * Launch an application by packageName and record usage.
     */
    fun launchApp(packageName: String): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                // Increment launch count
                val currentCount = prefs.getInt("launch_count_$packageName", 0)
                prefs.edit().putInt("launch_count_$packageName", currentCount + 1).apply()

                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                Log.w("Remmi", "[LauncherAppService] - Cannot find launch intent for $packageName")
                false
            }
        } catch (e: Exception) {
            Log.e("Remmi", "[LauncherAppService] - Failed to launch $packageName", e)
            false
        }
    }
}
