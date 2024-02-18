package com.example.myuiroom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.myuiroom.data.Database
import com.example.myuiroom.models.TaskModel
import com.example.myuiroom.repositories.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val notificationID = 1
const val channelID = "channel1"
const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"
const val nameChannel = "Notif Channel"

class Notification : BroadcastReceiver() {

private var taskRepository: TaskRepository? = null
    private var notificationIdTest: Int? = null
    lateinit var scheduleNotification: ScheduleNotification
companion object {
    const val ACTION_RESCHEDULE = "com.example.myuiroom.ACTION_RESCHEDULE"
    const val ACTION_COMPLETE = "com.example.myuiroom.ACTION_COMPLETE"
}
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {

        scheduleNotification = ScheduleNotification()
        when (intent.action) {
            ACTION_RESCHEDULE -> handleReschedule(context, intent)
            ACTION_COMPLETE -> handleComplete(context, intent)
            else -> showNotification(context, intent)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleReschedule(context: Context, intent: Intent) {
        // Extract task ID from intent and reschedule the task
        val taskId = intent.getStringExtra("idTask")?.toIntOrNull()

        taskId?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val task = getTaskFromDatabase(context, taskId)
                task?.let { taskModel ->
                    // Update taskModel as required for rescheduling
                   // taskModel.day += 1 // For example, incrementing the day by 1
                    val change = taskModel.minute + 2

                    taskModel.minute = change
                    updateTaskInDatabase(context, taskModel)
                    scheduleNotification.createNotification(context, taskModel, change)

                    val notificationId = taskModel.id.hashCode()
                    cancelNotification(context,  notificationId)
                }
            }
                    }
    }

    private suspend fun getTaskFromDatabase(context: Context, taskId: Int): TaskModel? {

        val taskDao = Database.getInstance(context).taskDao
        taskRepository = TaskRepository(taskDao)
        return taskRepository?.getTaskByIdDirect(taskId)
    }



    private fun handleComplete(context: Context, intent: Intent) {
        // Extract task ID from intent and reschedule the task

        val taskId = intent.getStringExtra("idTask")?.toIntOrNull()

        taskId?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val task = getTaskFromDatabase(context, taskId)
                task?.let { taskModel ->
                    taskModel.completed = "true" // For example, incrementing the day by 1
                    updateTaskInDatabase(context, taskModel)
                    val notificationId = taskModel.id.hashCode()
                    cancelNotification(context, notificationId)
               }
            }
        }
    }

    private fun updateTaskInDatabase(context: Context, taskModel: TaskModel) {

        val taskDao = Database.getInstance(context).taskDao
        val taskRepository = TaskRepository(taskDao)

        CoroutineScope(Dispatchers.IO).launch {
            taskRepository.updateTask(taskModel)
        }
    }

    private fun showNotification(context: Context, intent: Intent) {
        val idTask = intent.getStringExtra("idTask")
        val notificationId = idTask?.hashCode() ?: 0
        notificationIdTest = idTask?.hashCode() ?: 0

        val requestCodeReschedule = idTask.hashCode() // or some unique way to derive from taskId
        val requestCodeComplete = (idTask.hashCode() + 1)
        // Create the intent for opening the app (existing implementation)
        val openFragmentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("OpenFragment", "TaskForType")
            putExtra("idTask", idTask)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openFragmentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create the notification builder
        val notificationBuilder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.mipmap.ic_launcher_done_foreground)
            .setContentTitle(intent.getStringExtra(titleExtra))
            .setContentText(intent.getStringExtra(messageExtra))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        // Add action for rescheduling
        val rescheduleIntent = Intent(context, Notification::class.java).apply {
            action = ACTION_RESCHEDULE
            putExtra("idTask", idTask)
        }
        val reschedulePendingIntent = PendingIntent.getBroadcast(context, requestCodeReschedule, rescheduleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        notificationBuilder.addAction(0, "Transfer to 1 day", reschedulePendingIntent)
        // Add action for completing
        val completeIntent = Intent(context, Notification::class.java).apply {
            action = ACTION_COMPLETE
            putExtra("idTask", idTask)
        }
        val completePendingIntent = PendingIntent.getBroadcast(context, requestCodeComplete, completeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        notificationBuilder.addAction(0, "Done", completePendingIntent)
        // Build and show the notification
        val notification = notificationBuilder.build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(requestCodeReschedule, notification)
    }
    private fun cancelNotification(context: Context, taskId: Int) {
        val notificationId = taskId.hashCode()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationIdTest!!)
    }
}
