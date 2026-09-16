package com.remmi.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.remmi.app.core.database.converters.RemmiConverters
import com.remmi.app.plugins.tasks.data.local.dao.TaskDao
import com.remmi.app.plugins.tasks.data.local.entities.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RemmiConverters::class)
abstract class RemmiDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "remmi_database"
    }
}
