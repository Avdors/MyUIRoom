package com.example.myuiroom.viewModels

import android.text.BoringLayout
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myuiroom.models.FileModel
import com.example.myuiroom.models.TaskModel
import com.example.myuiroom.repositories.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class TaskViewModel(private val taskRepository: TaskRepository): ViewModel() {
    val tasks = taskRepository.tasks


    fun getFilter(type: String, completed: String, email: String): LiveData<List<TaskModel>> {
        return taskRepository.getFilter(type, completed)
    }

    fun startInsert(
        nameTask: String, email: String, typeTask: String, infoTask: String,
        dateStart: String, dateEnd: String, completed: String,
        day: Int, hourOfDay: Int, minute: Int, show_alert: String, category: String = "everything",
        onTaskInserted: (Long) -> Unit
    ) {
        val taskModel = TaskModel(
            0, nameTask, infoTask, email, typeTask, dateStart, dateEnd,
            completed, day, hourOfDay, minute, show_alert, category
        )
        viewModelScope.launch {
            val id = taskRepository.insertTask(taskModel)
            onTaskInserted(id)
        }
    }

    fun startInsertModel(taskModel: TaskModel) {
      //  insertTask(taskModel)
    }

    private fun insertTask(taskModel: TaskModel, onTaskInserted: (Long) -> Unit){
        Log.d("MyLog", "TaskViewModel test startInsertModel, day ${taskModel.day}, hourOfDay ${taskModel.hourOfDay}, minute ${taskModel.minute}, show_alert ${taskModel.show_alert}")
        viewModelScope.launch{
            val id = taskRepository.insertTask(taskModel)
            onTaskInserted(id)
        }
    }

    fun loadUniqueFiles(taskId: Int, taskModel: TaskModel){
        Log.d("SQLLog", "TaskViewModel loadUniqueFiles $taskId")
        //var returnValue = false
        viewModelScope.launch {

        val uniqueFiles = taskRepository?.getUniqueFilesForTask(taskId, taskModel)
            Log.d("SQLLog", "TaskViewModel uniqueFiles $uniqueFiles")
            deleteFiles(uniqueFiles!!, taskModel)
           // deleteFileRecords(uniqueFiles!!, taskModel)

        }

        //return returnValue

    }
    private fun deleteFiles(files: List<FileModel>, taskModel: TaskModel) {
        var returnValue = false
        files.forEach { file ->
            Log.d("SQLLog", "TaskViewModel deleteFiles file.filePath ${file.filePath}")
            val fileToDelete = File(file.filePath)
            Log.d("SQLLog", "fileToDelete.exists() ${fileToDelete.exists()}")
            if (fileToDelete.exists()) {
                returnValue = fileToDelete.delete()
                Log.d("SQLLog", "TaskViewModel deleteFiles fileToDelete.delete() ${returnValue}")
            }
        }
        deleteFileRecords(files, taskModel)


    }
    private fun deleteFileRecords(files: List<FileModel>, taskModel: TaskModel) {
        viewModelScope.launch{
            files.forEach { file ->
                taskRepository?.deleteFile(file)
            }
           // taskRepository.deleteTask(taskModel)
        }
    }

    fun startUpdateTask(idTask: Int, nameTask: String,email:String, typeTask: String, infoTask:String,
                        dateStart:String, dateEnd:String, completed: String, day : Int = 0, hourOfDay: Int = 0, minute: Int = 0, show_alert: String = "", category: String){
        updateTask(TaskModel(idTask,nameTask,infoTask,email,typeTask,dateStart,dateEnd,completed, day, hourOfDay, minute, show_alert, category))
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

    fun getTaskById(taskId: Int): LiveData<TaskModel>{

        return taskRepository.getTaskById(taskId)
    }

    fun deleteTaskById(taskId: Int){

        taskRepository.deleteTaskById(taskId)
    }

    fun getTaskCount() {
        viewModelScope.launch {

            taskRepository.getTaskCount()
        }
    }

    fun getFileCount() {
        viewModelScope.launch {

            taskRepository.getFileCount()
        }
    }

}