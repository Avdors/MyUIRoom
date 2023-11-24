package com.example.myuiroom.tabs

import android.app.*

import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myuiroom.*
import com.example.myuiroom.Notification

import com.example.myuiroom.data.Database
import com.example.myuiroom.databinding.PanelEditTaskBinding
import com.example.myuiroom.notices.*
import com.example.myuiroom.repositories.TaskRepository
import com.example.myuiroom.viewModels.TaskFactory
import com.example.myuiroom.viewModels.TaskViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*



    @RequiresApi(Build.VERSION_CODES.O)
    class PanelEditTask : BottomSheetDialogFragment(), View.OnClickListener{
    private var binding: PanelEditTaskBinding? = null
    private var nameFormParent: String? = ""
    private var taskAction: String? = ""
    private var complet: String? = null

    private var idTask: Int? = null
    private var email: String? = "test"
    private var typeTask: String? = null

    private var taskRepository: TaskRepository? = null
    private var taskViewModel: TaskViewModel? = null
    private var taskFactory: TaskFactory? = null

    private var firstStart: Boolean = true
    private var currentDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDate.now()
    } else {
        TODO("VERSION.SDK_INT < O")
    }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog)
        }

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = PanelEditTaskBinding.inflate(inflater, container, false)
        taskAction = arguments?.getString("taskAction").toString()
        nameFormParent = arguments?.getString("nameForm").toString()
        binding?.DateStartTask?.setText(arguments?.getString("dateStart").toString())
        complet = arguments?.getString("completed").toString()
        if (complet == "true"){

            binding?.checkBoxCompleted?.isChecked = true
            binding?.DateEndTask?.setText(arguments?.getString("dateEnd").toString())
        }
        else {
            binding?.DateEndTask?.setText(getString(R.string.enter_end_start))
            binding?.checkBoxCompleted?.isChecked = false}
        if(taskAction == "Edit") {
            idTask = arguments?.getString("idTask")?.toInt()
            binding?.editNameTask?.setText(arguments?.getString("nameTask").toString())
            email = arguments?.getString("email")?.toString()
            typeTask = arguments?.getString("typeTask")?.toString()
            binding?.editInfoTask?.setText(arguments?.getString("infoTask").toString())

        }
        else{
            val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val savedValue = sharedPreferences.getString("email", "default value")
            if(savedValue != "default value") {
                email = savedValue
            }
        }

        val taskDao = Database.getInstance((context as FragmentActivity).application).taskDao
        taskRepository = TaskRepository(taskDao)
        taskFactory = TaskFactory(taskRepository!!)
        taskViewModel = ViewModelProvider(this, taskFactory!!).get(TaskViewModel::class.java)
        spinerProcessing()


       // createNotification()
        binding?.finishEdit?.setOnClickListener(this)


        binding?.checkBoxCompleted?.setOnCheckedChangeListener{ buttonView, isChecked ->
          if(isChecked){
              binding?.DateEndTask?.setText(currentDate.toString())
          }

          else {
              binding?.DateEndTask?.setText(getString(R.string.enter_end_start))}
        }

        binding?.DateEndTask?.setOnClickListener{
            showDatePickerDialog("End")
        }

        binding?.DateStartTask?.setOnClickListener{
            showDatePickerDialog("Start")
        }
      //  createNotification()
        binding?.greateNotif?.setOnClickListener(this)

        val numberPicker = binding?.numberPickerDays
        numberPicker?.minValue = 1
        numberPicker?.maxValue = 365
        numberPicker?.value = 1



        return binding?.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePickerDialog(type:String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        var dateTextView: AppCompatTextView? = null

        if(type == "End"){
        dateTextView = binding?.DateEndTask as AppCompatTextView
        }
        else if(type == "Start"){
            dateTextView = binding?.DateStartTask as AppCompatTextView
        }

        if(dateTextView != null) {
            val datePickerDialog = DatePickerDialog(
                context as FragmentActivity,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                    val formattedDate = selectedDate.format(dateFormatter)
                    dateTextView.setText(formattedDate)
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }
    }

    fun spinerProcessing(){
        val taskTypeSpinner = binding?.taskTypeSpinner
        val taskTypes = resources.getStringArray(R.array.type)
        val adapter = ArrayAdapter(context as FragmentActivity, android.R.layout.simple_spinner_item, taskTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        taskTypeSpinner?.adapter = adapter

        if (firstStart && typeTask != null){
            val position = adapter.getPosition(typeTask)
            taskTypeSpinner?.setSelection(position)
            firstStart = false
        }

        taskTypeSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                typeTask = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
    }
    override fun onClick(view: View) {
    when (view.id) {
    R.id.finishEdit -> {
       if (binding?.checkBoxCompleted?.isChecked == true) {
           complet = "true"
       } else {
           complet = ""
       }
       if (binding?.editNameTask?.text?.toString()!!.isNotEmpty() and
           email.toString().isNotEmpty() and typeTask.toString().isNotEmpty() and
           binding?.editInfoTask?.text?.toString()!!.isNotEmpty() and
           binding?.DateStartTask?.text?.toString()!!.isNotEmpty() and
           binding?.DateEndTask?.text?.toString()!!.isNotEmpty()
       ) {

           if (nameFormParent == "TaskAll" && taskAction != "Edit") {

               taskViewModel?.startInsert(
                   binding?.editNameTask?.text?.toString()!!,
                   email.toString(),
                   typeTask.toString(),
                   binding?.editInfoTask?.text?.toString()!!,
                   binding?.DateStartTask?.text?.toString()!!,
                   binding?.DateEndTask?.text?.toString()!!,
                   complet.toString()
               )
               dismiss()
               (context as FragmentActivity).supportFragmentManager.beginTransaction()
                   .replace(R.id.content, TaskAll()).commit()
           } else if (nameFormParent == "TaskAll" && taskAction == "Edit") {
               if (idTask != null) {
                   taskViewModel?.startUpdateTask(
                       idTask?.toInt()!!,
                       binding?.editNameTask?.text?.toString()!!,
                       email.toString(),
                       typeTask.toString(),
                       binding?.editInfoTask?.text?.toString()!!,
                       binding?.DateStartTask?.text?.toString()!!,
                       binding?.DateEndTask?.text?.toString()!!,
                       complet.toString()
                   )
               }
               dismiss()
               (context as FragmentActivity).supportFragmentManager.beginTransaction()
                   .replace(R.id.content, TaskAll()).commit()
           } else if (nameFormParent == "TaskForType" && taskAction == "Edit") {
               if (idTask != null) {
                   taskViewModel?.startUpdateTask(
                       idTask?.toInt()!!,
                       binding?.editNameTask?.text?.toString()!!,
                       email.toString(),
                       typeTask.toString(),
                       binding?.editInfoTask?.text?.toString()!!,
                       binding?.DateStartTask?.text?.toString()!!,
                       binding?.DateEndTask?.text?.toString()!!,
                       complet.toString()
                   )
               }
               dismiss()
               (context as FragmentActivity).supportFragmentManager.beginTransaction()
                   .replace(R.id.content, TaskForType()).commit()
           } else if (nameFormParent == "TaskForType" && taskAction != "Edit") {

               taskViewModel?.startInsert(
                   binding?.editNameTask?.text?.toString()!!,
                   email.toString(),
                   typeTask.toString(),
                   binding?.editInfoTask?.text?.toString()!!,
                   binding?.DateStartTask?.text?.toString()!!,
                   binding?.DateEndTask?.text?.toString()!!,
                   complet.toString()
               )
               dismiss()

               (context as FragmentActivity).supportFragmentManager.beginTransaction()
                   .replace(R.id.content, TaskForType()).commit()
           }


       } else
           Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
   }
    R.id.greateNotif -> {
        Log.d("MyLog", "greateNotif: ")
        val intent = Intent(context, Notification::class.java)
        intent.setAction("com.example.myuiroom.MY_ACTION")
        val title = binding?.editNameTask?.text.toString()
        val message = getString(R.string.close_task)
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = getTime()
//        val result_SCHEDULE = context?.checkCallingOrSelfPermission("android.permission.SCHEDULE_EXACT_ALARM")
//        Log.d("MyLog", "test SCHEDULE_EXACT_ALARM, premission $result_SCHEDULE ")
//        val result_POST = context?.checkCallingOrSelfPermission("android.permission.POST_NOTIFICATIONS")
//        Log.d("MyLog", "test result_POST, premission $result_POST ")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )


        showAlert(time, title, message)
        dismiss()
    }
}

    }

    private fun showAlert(time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(requireContext())
        val timeFormat = android.text.format.DateFormat.getTimeFormat(requireContext())

        AlertDialog.Builder(context)
            .setTitle("Notification Scheuled")
            .setMessage(
                "Title: " + title +
                        "\nMessage: " + message +
                        "\nAt: " + dateFormat.format(date) + " " + timeFormat.format(date)
            )
            .setPositiveButton("Okay"){_,_ ->}
            .show()
    }

    private fun getTime(): Long {
        val minute = binding?.timePicker?.minute
        val hour = binding?.timePicker?.hour
        val day = binding?.datePicker?.dayOfMonth
        val month = binding?.datePicker?.month
        val year = binding?.datePicker?.year

        val calendar = Calendar.getInstance()
        calendar.set(year!!, month!!,day!!,hour!!,minute!!)
        return calendar.timeInMillis

    }
}