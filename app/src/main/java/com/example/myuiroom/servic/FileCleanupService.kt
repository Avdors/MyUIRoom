package com.example.myuiroom.servic

import android.app.Service
import android.app.Service.START_NOT_STICKY
import android.content.Intent
import android.os.IBinder
import com.example.myuiroom.data.Database
import com.example.myuiroom.data.TaskDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FileCleanupService: Service() {
    private lateinit var taskDao: TaskDao
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        taskDao = Database.getInstance(applicationContext).taskDao
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        performFileCleanup()
        return START_NOT_STICKY
    }

    private fun performFileCleanup() = CoroutineScope(Dispatchers.IO).launch {
        val filesDir = applicationContext.filesDir
        val files = filesDir.listFiles() ?: return@launch

        val filePathsInDb = getFilePathsFromDatabase()

        files.forEach { file ->
            if (file.name != "profileinstal" && !filePathsInDb.contains(file.absolutePath)) {
                file.delete()
            }
        }
    }

    private suspend fun getFilePathsFromDatabase(): List<String> = withContext(Dispatchers.IO) {
        taskDao.getAllFileModels()
    }
}