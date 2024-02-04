package com.example.myuiroom.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myuiroom.models.FileModel
import com.example.myuiroom.models.TaskModel


@Dao
interface TaskDao {

    @Insert
    suspend fun insertTask(taskModel: TaskModel): Long
    @Update
    suspend fun updateTask(taskModel: TaskModel)
    @Query("SELECT * FROM task_data_table")
    fun getAllTask(): LiveData<List<TaskModel>>
    @Query("SELECT * FROM task_data_table")
    suspend fun getAllTasksList(): List<TaskModel>
    @Query("SELECT * FROM task_data_table WHERE task_type == :type AND task_completed == :completed")
    fun getFilter(type:String, completed:String): LiveData<List<TaskModel>>
    @Delete
    suspend fun deleteTask(taskModel: TaskModel)
    @Query("DELETE FROM task_data_table")
    suspend fun deleteAllTask()
    @Query("SELECT * FROM task_data_table WHERE task_id = :taskId")
    fun getTaskById(taskId: Int): LiveData<TaskModel>
    @Query("DELETE FROM task_data_table WHERE task_id = :taskId")
    fun deleteTaskById(taskId: Int)
    @Query("SELECT * FROM task_data_table WHERE task_id = :taskId")
    suspend fun getTaskByIdDirect(taskId: Int): TaskModel?

    @Transaction
    @Query("SELECT * FROM task_data_table WHERE task_id = :taskId")
    fun getTaskWithFiles(taskId: Int): LiveData<TaskWithFiles>

    @Transaction
    @Query("SELECT * FROM file_data_table WHERE task_id = :taskId")
    suspend fun getFilesByTaskId(taskId: Int): MutableList<FileModel>
    // New function to get all files that are not linked to other tasks
    @Query("SELECT * FROM file_data_table WHERE file_path IN (:filePaths) AND task_id != :taskId")
    suspend fun getUniqueFiles(filePaths: List<String>, taskId: Int): MutableList<FileModel>

    data class TaskWithFiles(
        @Embedded val task: TaskModel,
        @Relation(
            parentColumn = "task_id",
            entityColumn = "task_id"
        )
        val files: List<FileModel>
    )

    @Insert
    suspend fun insertFile(fileModel: FileModel)
    @Delete
    suspend fun deleteFile(fileModel: FileModel)

    @Query("SELECT COUNT(*) FROM task_data_table")
    suspend fun getTaskCount(): Int

    @Query("SELECT COUNT(*) FROM file_data_table")
    suspend fun getFileCount(): Int

    @Query("SELECT file_path FROM file_data_table")
    suspend fun getAllFileModels(): List<String>
}