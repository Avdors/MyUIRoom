package com.example.myuiroom.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myuiroom.data.TaskDao
import com.example.myuiroom.repositories.TaskRepository

class FilesViewModel(private val repository: TaskRepository) : ViewModel() {

    fun getTaskWithFiles(taskId: Int): LiveData<TaskDao.TaskWithFiles> {
        return repository.getTaskWithFiles(taskId)
    }



    // Add methods for adding and removing files
}

class FilesViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FilesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FilesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}