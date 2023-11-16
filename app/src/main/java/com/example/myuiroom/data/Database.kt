package com.example.myuiroom.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myuiroom.models.TaskModel


@Database(entities = [TaskModel::class], version = 1)
abstract class Database: RoomDatabase() {
    abstract val taskDao: TaskDao
companion object{
    @Volatile
    private var INSTANCE : com.example.myuiroom.data.Database? = null
    fun getInstance(context: Context): com.example.myuiroom.data.Database{
        synchronized(this){
            var instance = INSTANCE
            if(instance==null){
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    com.example.myuiroom.data.Database::class.java,
                    "database"
                ).build()
            }
            return instance
        }
    }
}
}