package com.example.myuiroom

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.myuiroom.models.TaskModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class ScheduleNotification {
    private val dateFormatter: DateTimeFormatter
    init {
        dateFormatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .toFormatter()
    }
    fun createNotification(context: Context, task: TaskModel, change:Int = 0) {

        var dayTask = task.day.toString().toIntOrNull() ?: 0
        val hourTask = task.hourOfDay.toString().toIntOrNull() ?: 12
        var minuteTask = task.minute.toString().toIntOrNull() ?: 0
        if(change > 0){
            // временно меняю на минуты для теста
            dayTask = change


        }

        val intent = Intent(context, Notification::class.java)
        intent.setAction("com.example.myuiroom.MY_ACTION")
        val title = context.getString(R.string.close_task)
        val message = task.name.toString()
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)
        intent. putExtra("idTask", task.id.toString())

        val notificationId = task.id.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val time = getTime(task.dateStart, dayTask, hourTask, minuteTask)


        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )

    }
    fun createNotifForTime(context: Context, taskId: Int, time: Long, taskName: String) {



        val intent = Intent(context, Notification::class.java)
        intent.setAction("com.example.myuiroom.MY_ACTION")
        val title = context.getString(R.string.close_task)
        val message = taskName.toString()
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)
        intent. putExtra("idTask", taskId.toString())

        val notificationId = taskId.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
       // val time = getTime(task.dateStart, dayTask, hourTask, minuteTask)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )

    }

    fun clearNotificationById(context: Context, taskId: Int) {

//        val notificationId = taskId.hashCode()
//        Log.d("MyLog", "clearNotificationById")
//        val intent = Intent(context, Notification::class.java)
//        intent.setAction(ACTION_CLEAR)
//       // val title = context.getString(R.string.close_task)
//       //  val message = taskName.toString()
//       // intent.putExtra(titleExtra, title)
//       // intent.putExtra(messageExtra, message)
//        intent. putExtra("idTask", notificationId.toString())
//
//        context.sendBroadcast(intent)
//
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            notificationId,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        alarmManager.cancel(pendingIntent)
//
//

        val notificationId = taskId.hashCode()
        Log.d("MyLog", "clearNotificationById $notificationId")

        // Create an Intent that matches the one used to schedule the notification.
        val intent = Intent(context, Notification::class.java).apply {
            // Ensure all the details here match those of the PendingIntent used to schedule the notification.
            action = "com.example.myuiroom.MY_ACTION" // Use the exact same action used when scheduling.
            putExtra("idTask", taskId.toString()) // Match any extras if they were used. This might not be necessary depending on your PendingIntent flags.
        }

        // Create a PendingIntent that matches the one used to schedule.
        // The request code and flags here should match exactly.
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId, // This should match the request code used when scheduling.
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Now cancel the scheduled notification.
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        Log.d("MyLog", "Scheduled notification canceled for task ID: $taskId")


    }
    private fun getTime(startDateText : String, dayTask: Int, hourTask: Int, minuteTask : Int): Long {

        val calendar = Calendar.getInstance()

        try {
            val parsedDate = LocalDate.parse(startDateText, dateFormatter)
            calendar.set(parsedDate.year, parsedDate.monthValue - 1, parsedDate.dayOfMonth)
        } catch (e: DateTimeParseException) {

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