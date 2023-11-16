package com.example.myuiroom.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myuiroom.repositories.TaskRepository

class TaskFactory constructor(private val taskRepository: TaskRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T{
        return if (modelClass.isAssignableFrom(TaskViewModel::class.java)){
            TaskViewModel(this.taskRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}