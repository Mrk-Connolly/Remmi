package com.remmi.app.testing.plugins.recipebook

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.recipebook.RecipeRepository
import com.remmi.app.core.model.recipebook.RecipeItem
import java.util.UUID

class RecipeDatabaseTest(
    private val repository: RecipeRepository,
    private val testRepository: DatabaseTestRepository
) : PluginDatabaseTest {
    override val pluginId: String = "recipe_book"

    override suspend fun runTests(): List<DatabaseTestLog> {
        val tester = GenericPluginTester(pluginId, repository, testRepository)
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        val item = RecipeItem(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            title = "Test Recipe",
            description = "Test Description"
        )
        
        val updated = item.copy(title = "Updated Test Recipe")
        
        return tester.runCrudFlow(item, updated)
    }
}
