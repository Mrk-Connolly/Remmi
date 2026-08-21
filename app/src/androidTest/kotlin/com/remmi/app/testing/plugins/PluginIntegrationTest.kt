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
        controller = RemmiController(appContext)
        controller.start()
        testRepo = DatabaseTestRepository(controller.databaseManager.service)
    }

    @Test
    fun testAllPluginsDatabase() = runTest {
        // This is essentially the logic from the old runDatabaseTests
        val alarmPlugin = controller.pluginManager.plugins["alarm"] as? com.remmi.app.plugins.alarm.AlarmPlugin
        alarmPlugin?.let { AlarmDatabaseTest(it.repository, testRepo).runTests() }

        val calendarPlugin = controller.pluginManager.plugins["calendar"] as? com.remmi.app.plugins.calendar.CalendarPlugin
        calendarPlugin?.let { CalendarDatabaseTest(it.repository, testRepo).runTests() }

        val contactPlugin = controller.pluginManager.plugins["contacts"] as? com.remmi.app.plugins.contacts.ContactPlugin
        contactPlugin?.let { ContactDatabaseTest(it.repository, testRepo).runTests() }

        val giftPlugin = controller.pluginManager.plugins["gift"] as? com.remmi.app.plugins.gift.GiftPlugin
        giftPlugin?.let { GiftDatabaseTest(it.repository, testRepo).runTests() }

        val ingredientPlugin = controller.pluginManager.plugins["ingredient_stock"] as? com.remmi.app.plugins.ingredients.IngredientPlugin
        ingredientPlugin?.let { IngredientDatabaseTest(it.repository, it.repositoryStock, it.repositoryBatch, testRepo).runTests() }

        val recipePlugin = controller.pluginManager.plugins["recipe_book"] as? com.remmi.app.plugins.recipebook.RecipePlugin
        recipePlugin?.let { RecipeDatabaseTest(it.repository, testRepo).runTests() }

        val tasksPlugin = controller.pluginManager.plugins["tasks"] as? com.remmi.app.plugins.tasks.TasksPlugin
        tasksPlugin?.let { TaskDatabaseTest(it.repository, testRepo).runTests() }
    }

    @Test
    fun testAllPluginsActions() = runTest {
        val manager = ActionTestManager(testRepo)
        
        // Register all action tests
        (controller.pluginManager.plugins["alarm"] as? com.remmi.app.plugins.alarm.AlarmPlugin)?.let { 
            manager.registerTest(AddAlarmActionTest(it.actions)) 
        }
        (controller.pluginManager.plugins["calendar"] as? com.remmi.app.plugins.calendar.CalendarPlugin)?.let { 
            manager.registerTest(AddCalendarEventActionTest(it.actions)) 
        }
        (controller.pluginManager.plugins["contacts"] as? com.remmi.app.plugins.contacts.ContactPlugin)?.let { 
            manager.registerTest(AddContactActionTest(it.actions)) 
        }
        (controller.pluginManager.plugins["gift"] as? com.remmi.app.plugins.gift.GiftPlugin)?.let { 
            manager.registerTest(AddGiftIdeaActionTest(it.actions)) 
        }
        (controller.pluginManager.plugins["ingredient_stock"] as? com.remmi.app.plugins.ingredients.IngredientPlugin)?.let { 
            manager.registerTest(AddIngredientActionTest(it.actions)) 
        }
        (controller.pluginManager.plugins["recipe_book"] as? com.remmi.app.plugins.recipebook.RecipePlugin)?.let { 
            manager.registerTest(AddRecipeActionTest(it.actions)) 
        }
        (controller.pluginManager.plugins["tasks"] as? com.remmi.app.plugins.tasks.TasksPlugin)?.let { 
            manager.registerTest(AddTaskActionTest(it.actions)) 
        }
        (controller.pluginManager.plugins["weather"] as? com.remmi.app.plugins.weather.WeatherPlugin)?.let { 
            manager.registerTest(FetchWeatherActionTest(it.actions)) 
        }

        manager.runAllWithExceptions()
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
