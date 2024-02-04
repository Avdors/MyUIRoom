package com.example.myuiroom.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myuiroom.models.FileModel
import com.example.myuiroom.models.TaskModel


@Database(entities = [TaskModel::class, FileModel::class], version = 4)
abstract class Database: RoomDatabase() {
    abstract val taskDao: TaskDao
    companion object {
        @Volatile
        private var INSTANCE: com.example.myuiroom.data.Database? = null

        // Define the migration strategy
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to the table
                database.execSQL("ALTER TABLE task_data_table ADD COLUMN task_day INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE task_data_table ADD COLUMN task_hourOfDay INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE task_data_table ADD COLUMN task_minute INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE task_data_table ADD COLUMN task_minute INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE task_data_table ADD COLUMN task_show_alert TEXT")
                database.execSQL("CREATE TABLE 'file_data_table' ('file_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'task_id' INTEGER NOT NULL, 'file_path' TEXT NOT NULL)")
            }
        }

        fun getInstance(context: Context): com.example.myuiroom.data.Database {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        com.example.myuiroom.data.Database::class.java,
                        "database"
                    )
                        .addMigrations(MIGRATION_1_2) // Apply the migration
                        .build()
                }
                return instance
            }
        }
    }
}