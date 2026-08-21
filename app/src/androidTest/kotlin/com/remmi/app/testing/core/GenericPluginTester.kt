package com.remmi.app.testing.core

import android.util.Log
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.plugin.repository.RemmiRepository
import java.util.UUID

/**
 * GENERIC PLUGIN TESTER
 * 
 * Helper class to run standard database tests on any RemmiRepository.
 */
class GenericPluginTester<T : RemmiModel>(
    private val pluginId: String,
    private val repository: RemmiRepository<T>,
    private val testRepository: DatabaseTestRepository
) {

    suspend fun runCrudFlow(exampleItem: T, updatedItem: T): List<DatabaseTestLog> {
        val results = mutableListOf<DatabaseTestLog>()

        // 1. Test INSERT
        results.add(testOperation("INSERT") {
            repository.add(exampleItem)
        })

        // 2. Test GET_ALL
        results.add(testOperation("GET_ALL") {
            val all = repository.getAll()
            if (all.none { it.id == exampleItem.id }) {
                throw IllegalStateException("Inserted item not found in getAll()")
            }
        })

        // 3. Test UPDATE
        results.add(testOperation("UPDATE") {
            repository.update(updatedItem)
        })

        // 4. Test DELETE
        results.add(testOperation("DELETE") {
            repository.remove(exampleItem.id)
        })

        // Log results to database
        results.forEach { testRepository.add(it) }

        return results
    }

    private suspend fun testOperation(operation: String, block: suspend () -> Unit): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        return try {
            block()
            DatabaseTestLog(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                pluginId = pluginId,
                operation = operation,
                status = TestStatus.SUCCESS
            )
        } catch (e: Exception) {
            Log.e("RemmiTest", "[$pluginId] Test failed for $operation: ${e.message}")
            DatabaseTestLog(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                pluginId = pluginId,
                operation = operation,
                status = TestStatus.FAILURE,
                errorMessage = e.message
            )
        }
    }
}
