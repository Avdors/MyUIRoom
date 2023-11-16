package com.example.myuiroom.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myuiroom.models.TaskModel
import com.example.myuiroom.repositories.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(private val taskRepository: TaskRepository): ViewModel() {
    val tasks = taskRepository.tasks


    fun getFilter(type: String, completed: String, email: String): LiveData<List<TaskModel>> {
        return taskRepository.getFilter(type, completed)
    }

    fun startInsert(nameTask: String,email:String, typeTask: String, infoTask:String,
                    dateStart:String, dateEnd:String, completed: String) {
        insertTask(TaskModel(0,nameTask,infoTask,email,typeTask,dateStart,dateEnd,completed))
    }

    fun startInsertModel(taskModel: TaskModel) {
        insertTask(taskModel)
    }

    private fun insertTask(taskModel: TaskModel) = viewModelScope.launch {
        taskRepository.insertTask(taskModel)
    }

    fun startUpdateTask(idTask: Int, nameTask: String,email:String, typeTask: String, infoTask:String,
                        dateStart:String, dateEnd:String, completed: String){
        updateTask(TaskModel(idTask,nameTask,infoTask,email,typeTask,dateStart,dateEnd,completed))
    }

    private fun updateTask(taskModel: TaskModel) = viewModelScope.launch  {
        taskRepository.updateTask(taskModel)
    }
    fun deleteTask(taskModel: TaskModel) = viewModelScope.launch{
        taskRepository.deleteTask(taskModel)
    }
    fun deleteAllTask() = viewModelScope.launch{
        taskRepository.deleteAllTask()
    }
}