package database_scripts

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

/**
 * DATABASE UPDATER SCRIPT
 * 
 * Standalone entry point to perform database operations.
 * Run this from the IDE by right-clicking and selecting 'Run'.
 */
object DatabaseUpdater {

    /** Database Location - Copied from SupabaseService.kt */
    private const val SUPABASE_URL = "https://lmgexteedqzchmjdagxn.supabase.co"

    /** Database Public Key - Copied from SupabaseService.kt */
    private const val SUPABASE_ANON_KEY = "sb_publishable_NHFmOe4l9Yhz8nbfZay_pg_fi5j6boy"

    private val client by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                autoLoadFromStorage = false
                autoSaveToStorage = false
                sessionManager = object : SessionManager {
                    override suspend fun saveSession(session: UserSession) {}
                    override suspend fun loadSession(): UserSession? = null
                    override suspend fun deleteSession() {}
                }
            }
            install(Postgrest)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        // Suppress Java Preferences warning
        Logger.getLogger("java.util.prefs").level = Level.OFF

        println("==========================================")
        println("--- Starting Database Update Script ---")
        println("==========================================")
        
        try {
            executeStartupScript()
            println("--- Database Update Successful ---")
        } catch (e: Throwable) {
            System.err.println("--- Database Update Failed! ---")
            System.err.println("Error: ${e.message}")
            e.printStackTrace()
            // Throwing the exception ensures the JVM exits with a non-zero code,
            // which causes Gradle to report BUILD FAILED.
            throw e
        }
    }

    /**
     * Reads the startup.sql file from resources and executes it via a stored procedure in Supabase.
     */
    private suspend fun executeStartupScript() {
        println("Loading startup.sql from resources...")
        
        val sqlContent = this::class.java.classLoader.getResource("startup.sql")?.readText()
            ?: throw Exception("SQL file 'startup.sql' not found in resources.")
        
        println("Executing SQL script (Length: ${sqlContent.length} characters)...")
        
        try {
            client.postgrest.rpc(
                function = "exec_sql",
                parameters = buildJsonObject {
                    put("sql_text", sqlContent)
                }
            )
            println("Successfully executed SQL script via 'exec_sql' RPC.")
        } catch (e: Exception) {
            println("Error executing SQL via RPC: ${e.message}")
            println("Make sure you have created the 'exec_sql' function in your Supabase SQL Editor.")
            throw e
        }
    }
}
