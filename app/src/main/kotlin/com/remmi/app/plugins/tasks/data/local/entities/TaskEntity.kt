package com.remmi.app.plugins.tasks.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.remmi.app.core.plugin.model.components.RepeatRule
import com.remmi.app.plugins.tasks.models.SubTask
import com.remmi.app.plugins.tasks.models.TaskItem
import kotlinx.datetime.Instant

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val created: Instant,
    var modified: Instant,
    val title: String,
    val description: String,
    val completed: Boolean,
    val completedAt: Instant?,
    val dueDate: Instant?,
    val isPriority: Boolean,
    val group: String?,
    val subgroup: String?,
    val repeat: RepeatRule?,
    val subTasks: List<SubTask>,
    val parentTask: String?,
    val reminders: List<String>,
    val relationships: List<String>,
    val createCalendar: Boolean,
    val createAlarm: Boolean,
    val userId: String?,
    val sourcePlugin: String?,
    val sourceItemId: String?
)

fun TaskEntity.toTaskItem() = TaskItem(
    id = id,
    created = created,
    modified = modified,
    title = title,
    description = description,
    completed = completed,
    completedAt = completedAt,
    dueDate = dueDate,
    isPriority = isPriority,
    group = group,
    subgroup = subgroup,
    repeat = repeat,
    subTasks = subTasks,
    parentTask = parentTask,
    reminders = reminders,
    relationships = relationships,
    createCalendar = createCalendar,
    createAlarm = createAlarm,
    userId = userId,
    sourcePlugin = sourcePlugin,
    sourceItemId = sourceItemId
)

fun TaskItem.toEntity() = TaskEntity(
    id = id,
    created = created,
    modified = modified,
    title = title,
    description = description,
    completed = completed,
    completedAt = completedAt,
    dueDate = dueDate,
    isPriority = isPriority,
    group = group,
    subgroup = subgroup,
    repeat = repeat,
    subTasks = subTasks,
    parentTask = parentTask,
    reminders = reminders,
    relationships = relationships,
    createCalendar = createCalendar,
    createAlarm = createAlarm,
    userId = userId,
    sourcePlugin = sourcePlugin,
    sourceItemId = sourceItemId
)
