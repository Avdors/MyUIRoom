package com.example.myuiroom.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myuiroom.data.TaskDao
import com.example.myuiroom.models.FileModel
import com.example.myuiroom.models.TaskModel

class TaskRepository(private val taskDao: TaskDao) {

    val tasks = taskDao.getAllTask()

    fun getFilter(type: String, completed: String): LiveData<List<TaskModel>> {
        return taskDao.getFilter(type, completed)
    }

    suspend fun deleteAllTaskCompleted(completed: String) {
        taskDao.deleteAllTaskCompleted(completed)
    }

    suspend fun insertTask(taskModel: TaskModel): Long{
        return taskDao.insertTask(taskModel)
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

    fun getTaskById(taskId: Int): LiveData<TaskModel> {
        return taskDao.getTaskById(taskId)
    }

    fun deleteTaskById(taskId: Int){
        taskDao.deleteTaskById(taskId)
    }

    suspend fun getTaskAll(): List<TaskModel> {
        return taskDao.getAllTasksList()
    }

    suspend fun getTaskByIdDirect(taskId: Int): TaskModel? {
        return taskDao.getTaskByIdDirect(taskId)
    }

    fun getTaskWithFiles(taskId: Int): LiveData<TaskDao.TaskWithFiles> {
        return taskDao.getTaskWithFiles(taskId)
    }

    suspend fun insertFile(fileModel: FileModel){
        taskDao.insertFile(fileModel)
    }

    suspend fun getUniqueFilesForTask(taskId: Int, taskModel: TaskModel): MutableList<FileModel> {
        // LiveData to hold the final result
        var filesForTask: MutableList<FileModel>
        var uniqueFiles: MutableList<FileModel>


        // Observe the first LiveData
        filesForTask = taskDao.getFilesByTaskId(taskId)

            // Extract file paths
        val filePaths = filesForTask.map { it.filePath }

        uniqueFiles = taskDao.getUniqueFiles(filePaths, taskId)
        //result.postValue(uniqueFiles)
        val removPaths = uniqueFiles.map { it.filePath}


        filesForTask.forEach { file ->
            if (uniqueFiles.isNotEmpty()) {
                if (removPaths.contains(file.filePath)) {
                    filesForTask.remove(file)

                }
            }
        }
        return filesForTask
    }

    suspend fun deleteFile(fileModel: FileModel){
       taskDao.deleteFile(fileModel)
    }

    suspend fun getTaskCount(){

        val taskCount = taskDao.getTaskCount()



    }

    suspend fun getFileCount(){

        val fileCount = taskDao.getFileCount()
        }
}