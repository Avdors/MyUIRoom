package com.example.myuiroom

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.myuiroom.data.Database
import com.example.myuiroom.models.TaskModel
import com.example.myuiroom.repositories.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
class BootCompletedReceiver : BroadcastReceiver()  {
    private var taskRepository: TaskRepository? = null
    private val dateFormatter: DateTimeFormatter
    private var scheduleNotification: ScheduleNotification? = null
    init {
        dateFormatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .toFormatter()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Restore notifications here
            scheduleNotification = ScheduleNotification()
            restoreNotifications(context)
        }
    }

    private fun restoreNotifications(context: Context) {
        // Fetch tasks from the database and schedule notifications
        // This process should ideally be asynchronous (e.g., using coroutines)
        CoroutineScope(Dispatchers.IO).launch {
            val tasks = getTasksFromDatabase(context) // Implement this method to fetch tasks
            Log.d("MyLog", "BootCompletedReceiver tasks: $tasks")
            tasks?.forEach { task ->
                if (task.show_alert == "true") {
                    Log.d("MyLog", "BootCompletedReceiver task.show_alert: ${task.show_alert}")
                    scheduleNotification?.createNotification(context, task, )
                }
            }
        }
    }



    private suspend fun getTasksFromDatabase(context: Context): List<TaskModel>? {
        val taskDao = Database.getInstance(context).taskDao
        taskRepository = TaskRepository(taskDao)
        return taskRepository?.getTaskAll() ?: emptyList()
    }

//    private fun scheduleNotification(context: Context, task: TaskModel) {
//
//        val dayTask = task.day.toString().toIntOrNull() ?: 0
//        val hourTask = task.hourOfDay.toString().toIntOrNull() ?: 12
//        val minuteTask = task.minute.toString().toIntOrNull() ?: 0
//
//        Log.d("MyLog", "greateNotif: ")
//        val intent = Intent(context, Notification::class.java)
//        intent.setAction("com.example.myuiroom.MY_ACTION")
//        val title = context.getString(R.string.close_task)
//        val message = task.name.toString()
//        intent.putExtra(titleExtra, title)
//        intent.putExtra(messageExtra, message)
//        intent. putExtra("idTask", task.id.toString())
//        Log.d("MyLog", "greateNotif: IdTask ${task.id.toString()}")
//
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            notificationID,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val time = getTime(task.dateStart, dayTask, hourTask, minuteTask)
//
//        alarmManager.setExactAndAllowWhileIdle(
//            AlarmManager.RTC_WAKEUP,
//            time,
//            pendingIntent
//        )
//
//    }

    private fun getTime(startDateText : String, dayTask: Int, hourTask: Int, minuteTask : Int): Long {

        val calendar = Calendar.getInstance()

        try {
            val parsedDate = LocalDate.parse(startDateText, dateFormatter)
            calendar.set(parsedDate.year, parsedDate.monthValue - 1, parsedDate.dayOfMonth)
        } catch (e: DateTimeParseException) {
            Log.e("PanelEditTask", "Error parsing start date: $startDateText", e)
            // Handle the error, perhaps by using the current date or notifying the user
        }
        calendar.add(Calendar.DAY_OF_MONTH, dayTask ?: 0)


        calendar.set(Calendar.HOUR_OF_DAY, hourTask ?: 12)
        calendar.set(Calendar.MINUTE, minuteTask ?: 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)


        return calendar.timeInMillis

    }
}