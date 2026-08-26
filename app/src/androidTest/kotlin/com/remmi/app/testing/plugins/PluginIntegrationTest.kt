package com.remmi.app.testing.plugins

import androidx.test.platform.app.InstrumentationRegistry
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.testing.core.*
import com.remmi.app.testing.plugins.alarm.*
import com.remmi.app.testing.plugins.calendar.*
import com.remmi.app.testing.plugins.contacts.*
import com.remmi.app.testing.plugins.gift.*
import com.remmi.app.testing.plugins.ingredients.*
import com.remmi.app.testing.plugins.recipebook.*
import com.remmi.app.testing.plugins.tasks.*
import com.remmi.app.testing.plugins.maps.*
import com.remmi.app.testing.plugins.weather.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import android.util.Log

class PluginIntegrationTest {

    private lateinit var controller: RemmiController
    private lateinit var testRepo: DatabaseTestRepository

    @Before
    fun setup() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        controller = RemmiController(appContext, MockDatabaseService())
        controller.start()
        testRepo = DatabaseTestRepository(controller.databaseManager.service)
    }

    @Test
    fun testAllPluginsDatabase() = runTest {
        // This is essentially the logic from the old runDatabaseTests
        val alarmPlugin = controller.pluginManager.plugins["alarm"] as? com.remmi.app.plugins.alarm.AlarmPlugin
        alarmPlugin?.let { AlarmDatabaseTest(it.repository as com.remmi.app.plugins.alarm.AlarmRepository, testRepo).runTests() }

        val calendarPlugin = controller.pluginManager.plugins["calendar"] as? com.remmi.app.plugins.calendar.CalendarPlugin
        calendarPlugin?.let { CalendarDatabaseTest(it.repository as com.remmi.app.plugins.calendar.CalendarRepository, testRepo).runTests() }

        val contactPlugin = controller.pluginManager.plugins["contacts"] as? com.remmi.app.plugins.contacts.ContactPlugin
        contactPlugin?.let { ContactDatabaseTest(it.repository as com.remmi.app.plugins.contacts.ContactRepository, testRepo).runTests() }

        val giftPlugin = controller.pluginManager.plugins["gift"] as? com.remmi.app.plugins.gift.GiftPlugin
        giftPlugin?.let { GiftDatabaseTest(it.repository as com.remmi.app.plugins.gift.GiftRepository, testRepo).runTests() }

        val ingredientPlugin = controller.pluginManager.plugins["ingredient_stock"] as? com.remmi.app.plugins.ingredients.IngredientPlugin
        ingredientPlugin?.let { IngredientDatabaseTest(
            it.repository as com.remmi.app.plugins.ingredients.repository.MetadataRepository,
            it.repositoryStock as com.remmi.app.plugins.ingredients.repository.StockRepository,
            it.repositoryBatch as com.remmi.app.plugins.ingredients.repository.BatchRepository,
            testRepo).runTests() }

        val recipePlugin = controller.pluginManager.plugins["recipe_book"] as? com.remmi.app.plugins.recipebook.RecipePlugin
        recipePlugin?.let { RecipeDatabaseTest(it.repository as com.remmi.app.plugins.recipebook.RecipeRepository, testRepo).runTests() }

        val tasksPlugin = controller.pluginManager.plugins["tasks"] as? com.remmi.app.plugins.tasks.TasksPlugin
        tasksPlugin?.let { TaskDatabaseTest(it.repository as com.remmi.app.plugins.tasks.TasksRepository, testRepo).runTests() }
    }

    /**
     * Diagnostic version to test a single plugin database integration
     */
    suspend fun testSpecificPluginDatabase(pluginId: String) {
        when (pluginId) {
            "alarm" -> (controller.pluginManager.plugins["alarm"] as? com.remmi.app.plugins.alarm.AlarmPlugin)?.let { 
                AlarmDatabaseTest(it.repository as com.remmi.app.plugins.alarm.AlarmRepository, testRepo).runTests() 
            } ?: throw IllegalStateException("Alarm plugin not found")
            
            "calendar" -> (controller.pluginManager.plugins["calendar"] as? com.remmi.app.plugins.calendar.CalendarPlugin)?.let { 
                CalendarDatabaseTest(it.repository as com.remmi.app.plugins.calendar.CalendarRepository, testRepo).runTests() 
            } ?: throw IllegalStateException("Calendar plugin not found")

            "contacts" -> (controller.pluginManager.plugins["contacts"] as? com.remmi.app.plugins.contacts.ContactPlugin)?.let { 
                ContactDatabaseTest(it.repository as com.remmi.app.plugins.contacts.ContactRepository, testRepo).runTests() 
            } ?: throw IllegalStateException("Contacts plugin not found")

            "gift" -> (controller.pluginManager.plugins["gift"] as? com.remmi.app.plugins.gift.GiftPlugin)?.let { 
                GiftDatabaseTest(it.repository as com.remmi.app.plugins.gift.GiftRepository, testRepo).runTests() 
            } ?: throw IllegalStateException("Gift plugin not found")

            "ingredient_stock" -> (controller.pluginManager.plugins["ingredient_stock"] as? com.remmi.app.plugins.ingredients.IngredientPlugin)?.let { 
                IngredientDatabaseTest(
                    it.repository as com.remmi.app.plugins.ingredients.repository.MetadataRepository,
                    it.repositoryStock as com.remmi.app.plugins.ingredients.repository.StockRepository,
                    it.repositoryBatch as com.remmi.app.plugins.ingredients.repository.BatchRepository,
                    testRepo).runTests() 
            } ?: throw IllegalStateException("Ingredients plugin not found")

            "recipe_book" -> (controller.pluginManager.plugins["recipe_book"] as? com.remmi.app.plugins.recipebook.RecipePlugin)?.let { 
                RecipeDatabaseTest(it.repository as com.remmi.app.plugins.recipebook.RecipeRepository, testRepo).runTests() 
            } ?: throw IllegalStateException("Recipe plugin not found")

            "tasks" -> (controller.pluginManager.plugins["tasks"] as? com.remmi.app.plugins.tasks.TasksPlugin)?.let { 
                TaskDatabaseTest(it.repository as com.remmi.app.plugins.tasks.TasksRepository, testRepo).runTests() 
            } ?: throw IllegalStateException("Tasks plugin not found")
        }
    }

    @Test
    fun testAllPluginsActions() = runTest {
        val manager = getActionManager()
        manager.runAllWithExceptions()
    }

    /**
     * Diagnostic version that reports failures but doesn't throw until the end of the manager run.
     */
    suspend fun testAllPluginsActionsDiagnostic() {
        val manager = getActionManager()
        val failures = manager.runDiagnostic()
        if (failures.isNotEmpty()) {
            throw IllegalStateException("Action Failures:\n${failures.joinToString("\n")}")
        }
    }

    private fun getActionManager(): ActionTestManager {
        val manager = ActionTestManager(testRepo)
        
        // Register all action tests
        (controller.pluginManager.plugins["alarm"] as? com.remmi.app.plugins.alarm.AlarmPlugin)?.let { 
            manager.registerTest(AddAlarmActionTest(it.actions)) 
            manager.registerTest(AlarmFullFlowActionTest(it.actions))
        }
        (controller.pluginManager.plugins["calendar"] as? com.remmi.app.plugins.calendar.CalendarPlugin)?.let { 
            manager.registerTest(AddCalendarEventActionTest(it.actions)) 
            manager.registerTest(CalendarFullFlowActionTest(it.actions))
        }
        (controller.pluginManager.plugins["contacts"] as? com.remmi.app.plugins.contacts.ContactPlugin)?.let { 
            manager.registerTest(AddContactActionTest(it.actions)) 
            manager.registerTest(ContactFullFlowActionTest(it.actions))
        }
        (controller.pluginManager.plugins["gift"] as? com.remmi.app.plugins.gift.GiftPlugin)?.let { 
            manager.registerTest(AddGiftIdeaActionTest(it.actions)) 
            manager.registerTest(GiftFullFlowActionTest(it.actions))
        }
        (controller.pluginManager.plugins["ingredient_stock"] as? com.remmi.app.plugins.ingredients.IngredientPlugin)?.let { 
            manager.registerTest(AddIngredientActionTest(it.actions)) 
            manager.registerTest(IngredientFullFlowActionTest(it.actions))
        }
        (controller.pluginManager.plugins["maps"] as? com.remmi.app.plugins.maps.MapsPlugin)?.let { 
            manager.registerTest(AddSavedLocationActionTest(it.actions)) 
            manager.registerTest(MapsFullFlowActionTest(it.actions))
        }
        (controller.pluginManager.plugins["recipe_book"] as? com.remmi.app.plugins.recipebook.RecipePlugin)?.let { 
            manager.registerTest(AddRecipeActionTest(it.actions)) 
            manager.registerTest(RecipeFullFlowActionTest(it.actions))
        }
        (controller.pluginManager.plugins["tasks"] as? com.remmi.app.plugins.tasks.TasksPlugin)?.let { 
            manager.registerTest(AddTaskActionTest(it.actions)) 
            manager.registerTest(TasksFullFlowActionTest(it.actions))
        }
        (controller.pluginManager.plugins["weather"] as? com.remmi.app.plugins.weather.WeatherPlugin)?.let { 
            manager.registerTest(FetchWeatherActionTest(it.actions)) 
        }
        return manager
    }

    @Test
    fun testSpecific_Calendar_AddEvent() = runTest {
        val plugin = controller.pluginManager.plugins["calendar"] as? com.remmi.app.plugins.calendar.CalendarPlugin
        plugin?.let {
            val result = AddCalendarEventActionTest(it.actions).execute()
            testRepo.add(result)
            if (result.status == TestStatus.FAILURE) throw IllegalStateException(result.errorMessage)
        }
    }

    @Test
    fun testSpecific_Recipe_AddRecipe() = runTest {
        val plugin = controller.pluginManager.plugins["recipe_book"] as? com.remmi.app.plugins.recipebook.RecipePlugin
        plugin?.let {
            val result = AddRecipeActionTest(it.actions).execute()
            testRepo.add(result)
            if (result.status == TestStatus.FAILURE) throw IllegalStateException(result.errorMessage)
        }
    }
}
