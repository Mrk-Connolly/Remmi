package com.remmi.app.testing.core

import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * PLUGIN DATABASE TEST
 * 
 * Interface for plugin-specific database test implementations.
 */
interface PluginDatabaseTest {
    val pluginId: String
    suspend fun runTests(): List<DatabaseTestLog>
}

/**
 * REMMI ACTION TEST
 * 
 * Interface for specific high-level action tests (e.g., "Add Recipe").
 */
interface RemmiActionTest {
    val name: String
    val pluginId: String
    suspend fun execute(): DatabaseTestLog
}

/**
 * DATABASE TEST LOG
 * 
 * Model for recording test results in the database.
 */
@Serializable
data class DatabaseTestLog(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    @SerialName("user_id")
    override val userId: String? = null,

    @SerialName("plugin_id")
    val pluginId: String,
    val operation: String, // INSERT, UPDATE, DELETE, GET_ALL
    val status: TestStatus,
    @SerialName("error_message")
    val errorMessage: String? = null
) : RemmiModel

@Serializable
enum class TestStatus {
    SUCCESS, FAILURE
}
