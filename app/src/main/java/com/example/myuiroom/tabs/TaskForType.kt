package com.example.myuiroom.tabs

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.myuiroom.Notification
import com.example.myuiroom.R
import com.example.myuiroom.adapters.TaskAdapter
import com.example.myuiroom.data.Database
import com.example.myuiroom.databinding.TaskAllBinding
import com.example.myuiroom.databinding.TaskForTypeBinding
import com.example.myuiroom.models.TaskModel
import com.example.myuiroom.repositories.TaskRepository
import com.example.myuiroom.tabs.move.Listener
import com.example.myuiroom.viewModels.TaskFactory
import com.example.myuiroom.viewModels.TaskViewModel
import java.time.LocalDate


class TaskForType : Fragment(), View.OnClickListener, Listener {
    private var binding: TaskForTypeBinding? = null
    private var taskRepository: TaskRepository? = null
    private var taskViewModel: TaskViewModel? = null
    private var taskFactory: TaskFactory? = null
    private var taskAdapterimp: TaskAdapter? = null
    private var taskAdapterurg: TaskAdapter? = null
    private var taskAdapterimpurg: TaskAdapter? = null
    private var taskAdapternotimpurg: TaskAdapter? = null
    private var email: String? = "test"
    private var createNotif = false
    private var time = ""
    private var title = ""
    private var message = ""
    private var panelLoad : LoadingData? = null
    private val currentDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDate.now()
    } else {
        TODO("VERSION.SDK_INT < O")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = TaskForTypeBinding.inflate(inflater, container, false)

        val taskDao = Database.getInstance((context as FragmentActivity).application).taskDao
        taskRepository = TaskRepository(taskDao)
        taskFactory = TaskFactory(taskRepository!!)
        taskViewModel = ViewModelProvider(this, taskFactory!!).get(TaskViewModel::class.java)
        binding?.buttonAddTask?.setOnClickListener(this)
        loadTask()

        YoYo.with(Techniques.RubberBand)
            .duration(700)
            .repeat(5)
            .playOn(binding?.buttonAddTask)
        binding?.tvImportant?.visibility = View.VISIBLE
        binding?.tvNotImportantAndUrgent?.visibility = View.VISIBLE
        binding?.tvUrgent?.visibility = View.VISIBLE
        binding?.tvImportantAndUrgent?.visibility = View.VISIBLE

        val notif = arguments?.getString("createNotif")

        if(notif == "true"){
            createNotif = true
            time = arguments?.getString("time").toString()
            title = arguments?.getString("title").toString()
            message = arguments?.getString("message").toString()
           // scheduleNotification()
        }


        return binding?.root
    }

//     @RequiresApi(Build.VERSION_CODES.M)
//    private fun scheduleNotification() {
//
//        Log.d("MyLog", "scheduleNotification: ")
//        val intent = Intent(context, Notification::class.java)
//       intent.setAction("com.example.myuiroom.MY_ACTION")
//       val title = binding?.editNameTask?.text.toString()
//        val message = getString(R.string.close_task)
//        intent.putExtra(titleExtra, title)
//        intent.putExtra(messageExtra, message)
//       // intent.putExtra("activity", MainActivity::class.java)
//
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            notificationID,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val time = getTime()
//        val result_SCHEDULE = context?.checkCallingOrSelfPermission("android.permission.SCHEDULE_EXACT_ALARM")
//        Log.d("MyLog", "test SCHEDULE_EXACT_ALARM, premission $result_SCHEDULE ")
//        val result_POST = context?.checkCallingOrSelfPermission("android.permission.POST_NOTIFICATIONS")
//        Log.d("MyLog", "test result_POST, premission $result_POST ")
//        //alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent)
//      // alarmManager.setExactAndAllowWhileIdle(
//      // val triggerTime = System.currentTimeMillis() + 10 * 1000
//            alarmManager.setExactAndAllowWhileIdle(
//                AlarmManager.RTC_WAKEUP,
//                time,
//                pendingIntent
//            )
//
//
//           showAlert(time, title, message)
//
//
//    }

    private fun loadTask() {
        taskViewModel?.tasks?.observe(viewLifecycleOwner, Observer {

            val types = resources.getStringArray(R.array.type)
            for((index, name) in types.withIndex()){
                initRecyclerTask(name)

            }

        })
    }

    private fun initRecyclerTask(type: String?) {
        if(type == "important"){
            binding?.important?.layoutManager = LinearLayoutManager(context)
            taskAdapterimp = TaskAdapter(this, {taskModel: TaskModel -> completeTask(taskModel)  },
                {taskModel: TaskModel -> editTask(taskModel)  })

            binding?.important?.adapter = taskAdapterimp
            filterTasks(type)
            binding?.tvImportant?.setOnDragListener(taskAdapterimp?.getDragInstance())
            binding?.important?.setOnDragListener(taskAdapterimp?.getDragInstance())
        }
        else if(type == "urgent"){
            binding?.urgent?.layoutManager = LinearLayoutManager(context)
            taskAdapterurg = TaskAdapter(this, {taskModel: TaskModel -> completeTask(taskModel)  },
                {taskModel: TaskModel -> editTask(taskModel)  })

            binding?.urgent?.adapter = taskAdapterurg
            filterTasks(type)
            binding?.tvUrgent?.setOnDragListener(taskAdapterurg?.getDragInstance())
            binding?.urgent?.setOnDragListener(taskAdapterurg?.getDragInstance())
        }
        else if(type == "importantAndUrgent"){
            binding?.importantAndUrgent?.layoutManager = LinearLayoutManager(context)
            taskAdapterimpurg = TaskAdapter(this, {taskModel: TaskModel -> completeTask(taskModel)  },
                {taskModel: TaskModel -> editTask(taskModel)  })

            binding?.importantAndUrgent?.adapter = taskAdapterimpurg
            filterTasks(type)
            binding?.tvImportantAndUrgent?.setOnDragListener(taskAdapterimpurg?.getDragInstance())
            binding?.importantAndUrgent?.setOnDragListener(taskAdapterimpurg?.getDragInstance())
        }
        else if(type == "notimportantAndUrgent"){
            binding?.notImportantAndUrgent?.layoutManager = LinearLayoutManager(context)
            taskAdapternotimpurg = TaskAdapter(this, {taskModel: TaskModel -> completeTask(taskModel)  },
                {taskModel: TaskModel -> editTask(taskModel)  })

            binding?.notImportantAndUrgent?.adapter = taskAdapternotimpurg
            filterTasks(type)
            binding?.tvNotImportantAndUrgent ?.setOnDragListener(taskAdapternotimpurg?.getDragInstance())
            binding?.notImportantAndUrgent?.setOnDragListener(taskAdapternotimpurg?.getDragInstance())
        }
    }

    private fun filterTasks(type: String) {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedValue = sharedPreferences.getString("email", "test")
        if(savedValue != "default value") {
            email = savedValue
        }

        val filteredList = taskViewModel?.tasks?.value?.filter { task ->
            task.type == type && task.email == email && task.completed != "true"
        }

        // для перетаскивания здесь определим какой ресайклер использовать
        val recyclerView = when (type) {
            "important" -> binding?.important
            "urgent" -> binding?.urgent
            "importantAndUrgent" -> binding?.importantAndUrgent
            "notimportantAndUrgent" -> binding?.notImportantAndUrgent
            else -> null
        }

        val taskAdapterin = when (type) {
            "important" -> taskAdapterimp
            "urgent" -> taskAdapterurg
            "importantAndUrgent" -> taskAdapterimpurg
            "notimportantAndUrgent" -> taskAdapternotimpurg
            else -> null
        }
        taskAdapterin?.setList(filteredList as List<TaskModel>)
        taskAdapterin?.notifyDataSetChanged()
    }

    private fun completeTask(taskModel: TaskModel) {
        val dateString = currentDate.toString()
        taskViewModel?.startUpdateTask(
            taskModel.id,
            taskModel.name,
            taskModel.email,
            taskModel.type,
            taskModel.info,
            taskModel.dateStart,
            dateString,
            "true"
        )

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun editTask(taskModel: TaskModel) {
        val panelEditTask = PanelEditTask()
        val parameters = Bundle()
        parameters.putString("idTask", taskModel.id.toString())
        parameters.putString("nameTask", taskModel.name.toString())
        parameters.putString("email", taskModel.email.toString())
        parameters.putString("typeTask", taskModel.type.toString())
        parameters.putString("infoTask", taskModel.info.toString())
        parameters.putString("dateStart", taskModel.dateStart.toString())
        parameters.putString("dateEnd", taskModel.dateEnd.toString())
        parameters.putString("completed", taskModel.completed.toString())
        parameters.putString("nameForm", "TaskForType")
        parameters.putString("taskAction", "Edit")
        panelEditTask.arguments = parameters

        panelEditTask.show((context as FragmentActivity).supportFragmentManager, "editTask")
    }

    override fun onClick(view: View) {

        val dateString = currentDate.toString()

        when(view.id){
            R.id.buttonAddTask -> {


                val panelEditTask = PanelEditTask()
                val parameters = Bundle()
                parameters.putString("dateStart", dateString)
                parameters.putString("completed", "")
                parameters.putString("nameForm", "TaskForType")
                parameters.putString("taskAction", "Add")
                panelEditTask.arguments = parameters
                panelEditTask.show((context as FragmentActivity).supportFragmentManager, "addTask")


            }

        }
    }
//изменения по новому адаптеру перетаскивания
    override fun setEmptyListImportant(visibility: Boolean) {
            binding?.tvImportant?.visibility = if (visibility) View.VISIBLE else View.GONE
            Log.d("MyLog", "setEmptyListImportant")

    }
    override fun setEmptyListNotImportant(visibility: Boolean) {
        binding?.tvNotImportantAndUrgent?.visibility = if (visibility) View.VISIBLE else View.GONE
            Log.d("MyLog", "setEmptyListNotImportant")

    }
    override fun setEmptyListUrgent(visibility: Boolean) {
        binding?.tvUrgent?.visibility = if (visibility) View.VISIBLE else View.GONE
            Log.d("MyLog", "setEmptyListUrgent")

    }
    override fun setEmptyListImportantUrgent(visibility: Boolean) {
        binding?.tvImportantAndUrgent?.visibility = if (visibility) View.VISIBLE else View.GONE
            Log.d("MyLog", "setEmptyListImportantUrgent")

    }

    override fun editTaskType(taskModel: TaskModel, type: String) {
        taskViewModel?.startUpdateTask(taskModel.id, taskModel.name, taskModel.email,
            type, taskModel.info, taskModel.dateStart, taskModel.dateEnd, taskModel.completed)
    }

}