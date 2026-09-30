package com.remmi.app.testing.plugins.exhaustive

import com.google.common.truth.Truth.assertThat
import com.remmi.app.plugins.tasks.TasksPlugin
import com.remmi.app.testing.base.BasePluginActionTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * TASKS EXHAUSTIVE TEST
 */
open class TasksExhaustiveTest : BasePluginActionTest() {

    @Test
    fun createTask_validInput_createsTogglesAndDeletesTask() = runTest {
        // Arrange
        val plugin = controller.pluginManager.plugins["tasks"] as TasksPlugin
        
        // Act 1: Create
        val success = plugin.actions.createTask(
            title = "Major Task",
            description = "Main task description",
            isPriority = true
        )

        // Assert 1
        assertThat(success).isTrue()
        var tasks = plugin.actions.getAllTasks()
        val task = tasks.find { it.title == "Major Task" }
        assertThat(task).isNotNull()
        assertThat(task!!.isPriority).isTrue()
        
        // Act 2: Toggle Task
        val toggleSuccess = plugin.actions.toggleTask(task)

        // Assert 2
        assertThat(toggleSuccess).isTrue()
        val updatedTask = plugin.actions.getTask(task.id)
        assertThat(updatedTask!!.completed).isTrue()
        assertThat(updatedTask.completedAt).isNotNull()
        
        // Act 3: Delete
        val deleteSuccess = plugin.actions.deleteTask(task.id)

        // Assert 3
        assertThat(deleteSuccess).isTrue()
        tasks = plugin.actions.getAllTasks()
        assertThat(tasks.any { it.id == task.id }).isFalse()
    }

    @Test
    fun createMultitask_validSubtitles_createsParentAndChildTasks() = runTest {
        // Arrange
        val plugin = controller.pluginManager.plugins["tasks"] as TasksPlugin
        
        // Act
        val success = plugin.actions.createMultitask(
            titles = listOf("Sub 1", "Sub 2"),
            description = "Group Desc",
            group = "Work",
            subgroup = "Project X",
            dueDate = null,
            isPriority = false,
            repeat = null,
            createAlarm = false,
            createCalendar = false
        )

        // Assert
        assertThat(success).isTrue()
        val allTasks = plugin.actions.getAllTasks()
        val parent = allTasks.find { it.title == "Project X" }
        assertThat(parent).isNotNull()
        
        val children = allTasks.filter { it.parentTask == parent!!.id }
        assertThat(children).hasSize(2)
        assertThat(children.map { it.title }).containsExactly("Sub 1", "Sub 2")
    }
}
