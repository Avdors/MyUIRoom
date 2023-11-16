package com.example.myuiroom.repositories

import androidx.lifecycle.LiveData
import com.example.myuiroom.data.TaskDao
import com.example.myuiroom.models.TaskModel

class TaskRepository(private val taskDao: TaskDao) {

    val tasks = taskDao.getAllTask()

    fun getFilter(type: String, completed: String): LiveData<List<TaskModel>> {
        return taskDao.getFilter(type, completed)
    }

    suspend fun insertTask(taskModel: TaskModel){
        taskDao.insertTask(taskModel)
    }

    suspend fun updateTask(taskModel: TaskModel){
        taskDao.updateTask(taskModel)
    }
    suspend fun deleteTask(taskModel: TaskModel){
        taskDao.deleteTask(taskModel)
    }
    suspend fun deleteAllTask(){
        taskDao.deleteAllTask()
    }
}