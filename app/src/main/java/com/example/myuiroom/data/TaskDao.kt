package com.example.myuiroom.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myuiroom.models.TaskModel


@Dao
interface TaskDao {

    @Insert
    suspend fun insertTask(taskModel: TaskModel)
    @Update
    suspend fun updateTask(taskModel: TaskModel)
    @Query("SELECT * FROM task_data_table")
    fun getAllTask(): LiveData<List<TaskModel>>
    @Query("SELECT * FROM task_data_table WHERE task_type == :type AND task_completed == :completed")
    fun getFilter(type:String, completed:String): LiveData<List<TaskModel>>
    @Update
    suspend fun deleteTask(taskModel: TaskModel)
    @Query("DELETE FROM task_data_table")
    suspend fun deleteAllTask()



}