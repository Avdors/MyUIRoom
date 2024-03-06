package com.example.myuiroom.tabs

import android.animation.Animator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myuiroom.R
import com.example.myuiroom.adapters.TaskAdapter
import com.example.myuiroom.data.Database
import com.example.myuiroom.databinding.TaskForTypeBinding
import com.example.myuiroom.models.TaskModel
import com.example.myuiroom.repositories.TaskRepository
import com.example.myuiroom.tabs.move.Listener
import com.example.myuiroom.viewModels.TaskFactory
import com.example.myuiroom.viewModels.TaskViewModel
import java.time.LocalDate


class TaskForType : Fragment(), View.OnClickListener, Listener, PanelEditTaskDismissListener {
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
    private var categoryStr: String? = null
    private var panelLoad : LoadingData? = null
    private var isPanelEditTaskOpen = false
    private val currentDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDate.now()
    } else {
        TODO("VERSION.SDK_INT < O")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = TaskForTypeBinding.inflate(inflater, container, false)
        checkBatteryOptimizations(requireContext())
        val taskDao = Database.getInstance((context as FragmentActivity).application).taskDao
        taskRepository = TaskRepository(taskDao)
        taskFactory = TaskFactory(taskRepository!!)
        taskViewModel = ViewModelProvider(this, taskFactory!!).get(TaskViewModel::class.java)
        binding?.buttonAddTask?.setOnClickListener(this)
        //если удаляем задачу из Панели редактирования
        val taskIdDelete = arguments?.getString("idTaskDelete")

        if (taskIdDelete != null) {
        //deleteTaskById(taskIdDelete)

        }
        loadTask()

        binding?.tvImportant?.visibility = View.VISIBLE
        binding?.tvNotImportantAndUrgent?.visibility = View.VISIBLE
        binding?.tvUrgent?.visibility = View.VISIBLE
        binding?.tvImportantAndUrgent?.visibility = View.VISIBLE
        binding?.laAnimDone?.setSpeed(1f)
        binding?.laAnimDone?.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                binding?.laAnimDone?.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {
                // Animation cancelled
            }

            override fun onAnimationRepeat(animation: Animator) {
                // Animation repeated
            }
        })

        val notif = arguments?.getString("createNotif")

        if(notif == "true"){
            createNotif = true
            time = arguments?.getString("time").toString()
            title = arguments?.getString("title").toString()
            message = arguments?.getString("message").toString()

        }

        //if open for notification
        val taskId = arguments?.getString("idTask")
        if (taskId != null) {

            findTaskById(taskId)
        }

        // обработка нажатия по иконкам категорий
        // Initialize buttons
        val ibFamily = binding?.ibFamily
        val ibJob = binding?.ibJob
        val ibMedical = binding?.ibHealth
        val ibTable = binding?.ibStudy
        val ibEverything = binding?.ibEverything
        // ... other buttons

        // Click listeners
        val buttons = listOf(ibFamily, ibJob, ibMedical, ibEverything, ibTable)
        var buttonSelected: AppCompatImageButton? = null
        buttons.forEach { button ->
            button?.setOnClickListener {
                // Deselect all buttons
                buttons.forEach {
                    if(it?.isSelected == true){

                        buttonSelected = it
                    }
                    it?.isSelected = false }

                // Select the clicked button

                if(button == buttonSelected){
                    button.isSelected = false
                    handleButtonClick(button.id)
                    buttonSelected = null
                    categoryStr = null
                }else {
                    button.isSelected = true
                    handleButtonClick(button.id)
                }


                loadTask()
                // Handle the button click event
              //  handleButtonClick(button.id)
            }
        }

        recordCheck()

        return binding?.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        loadTask()
        Log.d("MyLog", "onResume")
    }

    private fun recordCheck(){

        taskViewModel!!.getTaskCount()
        taskViewModel!!.getFileCount()
    }

    private fun handleButtonClick(buttonId: Int) {
        // Handle button click based on buttonId
        when (buttonId) {
            R.id.ib_family -> {
                categoryStr = "family"
            }
            R.id.ib_job -> {
                categoryStr = "job"
            }
            R.id.ib_study -> {
                categoryStr = "study"
            }
            R.id.ib_health -> {
                categoryStr = "health"
            }
            R.id.ib_everything -> {
                categoryStr = "everything"
            }

        }
    }

    private fun checkBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(requireContext().packageName)) {
                AlertDialog.Builder(context)
                    .setTitle("Disable Battery Optimizations")
                    .setMessage("Please disable battery optimizations for reliable notifications.")
                    .setPositiveButton("Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:" + requireContext().packageName)
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadTask() {
        taskViewModel?.tasks?.observe(viewLifecycleOwner, Observer {

            val types = resources.getStringArray(R.array.type)
            for((index, name) in types.withIndex()){
                initRecyclerTask(name)

            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initRecyclerTask(type: String?) {
        if(type == "important"){
            binding?.important?.layoutManager = LinearLayoutManager(context)
            taskAdapterimp = TaskAdapter(this, {taskModel: TaskModel -> completeTask(taskModel)  },
                {taskModel: TaskModel -> editTask(taskModel)  }, {taskModel: TaskModel -> deleteTask(taskModel)  },false)

            binding?.important?.adapter = taskAdapterimp
            filterTasks(type)
            binding?.tvImportant?.setOnDragListener(taskAdapterimp?.getDragInstance())
            binding?.important?.setOnDragListener(taskAdapterimp?.getDragInstance())
        }
        else if(type == "urgent"){
            binding?.urgent?.layoutManager = LinearLayoutManager(context)
            taskAdapterurg = TaskAdapter(this, {taskModel: TaskModel -> completeTask(taskModel)  },
                {taskModel: TaskModel -> editTask(taskModel)  }, {taskModel: TaskModel -> deleteTask(taskModel)  },false)

            binding?.urgent?.adapter = taskAdapterurg
            filterTasks(type)
            binding?.tvUrgent?.setOnDragListener(taskAdapterurg?.getDragInstance())
            binding?.urgent?.setOnDragListener(taskAdapterurg?.getDragInstance())
        }
        else if(type == "importantAndUrgent"){
            binding?.importantAndUrgent?.layoutManager = LinearLayoutManager(context)
            taskAdapterimpurg = TaskAdapter(this, {taskModel: TaskModel -> completeTask(taskModel)  },
                {taskModel: TaskModel -> editTask(taskModel)  }, {taskModel: TaskModel -> deleteTask(taskModel)  },false)

            binding?.importantAndUrgent?.adapter = taskAdapterimpurg
            filterTasks(type)
            binding?.tvImportantAndUrgent?.setOnDragListener(taskAdapterimpurg?.getDragInstance())
            binding?.importantAndUrgent?.setOnDragListener(taskAdapterimpurg?.getDragInstance())
        }
        else if(type == "notimportantAndUrgent"){
            binding?.notImportantAndUrgent?.layoutManager = LinearLayoutManager(context)
            taskAdapternotimpurg = TaskAdapter(this, {taskModel: TaskModel -> completeTask(taskModel)  },
                {taskModel: TaskModel -> editTask(taskModel)  }, {taskModel: TaskModel -> deleteTask(taskModel)  },false)

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

                if(categoryStr != null){

                    task.type == type && task.email == email && task.completed != "true"&& task.category == categoryStr
                }else{
                    task.type == type && task.email == email && task.completed != "true"
                }

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
            "true",0,0,0,"", taskModel.category
        )
    binding?.laAnimDone?.visibility = View.VISIBLE
    binding?.laAnimDone?.playAnimation()


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun editTask(taskModel: TaskModel) {
      if(!isPanelEditTaskOpen) {
          isPanelEditTaskOpen = true
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
          parameters.putString("dayTask", taskModel.day.toString())
          parameters.putString("hourTask", taskModel.hourOfDay.toString())
          parameters.putString("minuteTask", taskModel.minute.toString())
          parameters.putString("showAlertStr", taskModel.show_alert)
          parameters.putString("category", taskModel.category)
          parameters.putString("nameForm", "TaskForType")
          parameters.putString("taskAction", "Edit")
          panelEditTask.arguments = parameters

          panelEditTask.setDismissListener(this) // здесь смотрим обратный вызов, чтобы увидеть закрытие панели
          panelEditTask.show((context as FragmentActivity).supportFragmentManager, "editTask")


      }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun deleteTask(taskModel: TaskModel) {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(view: View) {

        val dateString = currentDate.toString()

        when(view.id){
            R.id.buttonAddTask -> {

                if(!isPanelEditTaskOpen) {
                    isPanelEditTaskOpen = true
                    val panelEditTask = PanelEditTask()
                    val parameters = Bundle()
                    parameters.putString("dateStart", dateString)
                    parameters.putString("completed", "")
                    parameters.putString("nameForm", "TaskForType")
                    parameters.putString("taskAction", "Add")
                    parameters.putString("category", "everything")
                    panelEditTask.arguments = parameters
                    panelEditTask.setDismissListener(this)
                    panelEditTask.show(
                        (context as FragmentActivity).supportFragmentManager,
                        "addTask"
                    )
                }
            }

        }
    }
//изменения по новому адаптеру перетаскивания
    override fun setEmptyListImportant(visibility: Boolean) {
            binding?.tvImportant?.visibility = if (visibility) View.VISIBLE else View.GONE


    }
    override fun setEmptyListNotImportant(visibility: Boolean) {
        binding?.tvNotImportantAndUrgent?.visibility = if (visibility) View.VISIBLE else View.GONE


    }
    override fun setEmptyListUrgent(visibility: Boolean) {
        binding?.tvUrgent?.visibility = if (visibility) View.VISIBLE else View.GONE


    }
    override fun setEmptyListImportantUrgent(visibility: Boolean) {
        binding?.tvImportantAndUrgent?.visibility = if (visibility) View.VISIBLE else View.GONE


    }

    override fun editTaskType(taskModel: TaskModel, type: String) {
        taskViewModel?.startUpdateTask(taskModel.id, taskModel.name, taskModel.email,
            type, taskModel.info, taskModel.dateStart, taskModel.dateEnd, taskModel.completed, taskModel.day, taskModel.hourOfDay, taskModel.minute, taskModel.show_alert, taskModel.category)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun findTaskById(taskId: String) {

        val taskIdInt = taskId.toIntOrNull() ?: return
        taskViewModel?.getTaskById(taskIdInt)?.observe(viewLifecycleOwner, Observer { taskModel ->
            // Assuming taskModel is the TaskModel you want to edit

            taskModel?.let { editTask(it)
          }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun deleteTaskById(taskId: String) {

        val taskIdInt = taskId.toIntOrNull() ?: return
        taskViewModel?.deleteTaskById(taskIdInt)
    }

    override fun onPanelEditTaskDismissed() {
        isPanelEditTaskOpen = false
    }

}