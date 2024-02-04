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

        Log.d("SQLLog", "TaskRepository getUniqueFilesForTask $taskId")
        // Observe the first LiveData
        filesForTask = taskDao.getFilesByTaskId(taskId)
        Log.d("SQLLog", "TaskRepository filesForTask $filesForTask")
            // Extract file paths
        val filePaths = filesForTask.map { it.filePath }
        Log.d("SQLLog", "TaskRepository filePaths $filePaths")
        uniqueFiles = taskDao.getUniqueFiles(filePaths, taskId)
        //result.postValue(uniqueFiles)
        val removPaths = uniqueFiles.map { it.filePath}
        Log.d("SQLLog", "TaskRepository uniqueFiles $uniqueFiles")

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
        Log.d("SQLLog", "TaskRepository deleteFile $fileModel")
        taskDao.deleteFile(fileModel)
    }

    suspend fun getTaskCount(){

        val taskCount = taskDao.getTaskCount()
        Log.d("SQLLog", "TaskRepository taskCount $taskCount")


    }

    suspend fun getFileCount(){

        val fileCount = taskDao.getFileCount()
        Log.d("SQLLog", "TaskRepository fileCount $fileCount")
        }
}