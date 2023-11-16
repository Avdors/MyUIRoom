package com.example.myuiroom.tabs

import android.content.Context
import android.os.Build
import android.os.Bundle
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
import com.example.myuiroom.R
import com.example.myuiroom.adapters.TaskAdapter
import com.example.myuiroom.data.Database
import com.example.myuiroom.databinding.TaskAllBinding
import com.example.myuiroom.databinding.TaskForTypeBinding
import com.example.myuiroom.models.TaskModel
import com.example.myuiroom.repositories.TaskRepository
import com.example.myuiroom.viewModels.TaskFactory
import com.example.myuiroom.viewModels.TaskViewModel
import java.time.LocalDate

//, DragManageAdapter, OnItemMoveListener
class TaskForType : Fragment(), View.OnClickListener {
    private var binding: TaskForTypeBinding? = null
    private var taskRepository: TaskRepository? = null
    private var taskViewModel: TaskViewModel? = null
    private var taskFactory: TaskFactory? = null
    private var taskAdapterimp: TaskAdapter? = null
    private var taskAdapterurg: TaskAdapter? = null
    private var taskAdapterimpurg: TaskAdapter? = null
    private var taskAdapternotimpurg: TaskAdapter? = null
   // private var taskAdapter: DragManageAdapter? = null
    private var email: String? = "test"
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
        return binding?.root
    }

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
            taskAdapterimp = TaskAdapter({taskModel: TaskModel -> completeTask(taskModel)  },
                {taskModel: TaskModel -> editTask(taskModel)  })

            binding?.important?.adapter = taskAdapterimp
            filterTasks(type)
        }
        else if(type == "urgent"){
            binding?.urgent?.layoutManager = LinearLayoutManager(context)
            taskAdapterurg = TaskAdapter({taskModel: TaskModel -> completeTask(taskModel)  },
                {taskModel: TaskModel -> editTask(taskModel)  })

            binding?.urgent?.adapter = taskAdapterurg
            filterTasks(type)
        }
        else if(type == "importantAndUrgent"){
            binding?.importantAndUrgent?.layoutManager = LinearLayoutManager(context)
            taskAdapterimpurg = TaskAdapter({taskModel: TaskModel -> completeTask(taskModel)  },
                {taskModel: TaskModel -> editTask(taskModel)  })

            binding?.importantAndUrgent?.adapter = taskAdapterimpurg
            filterTasks(type)
        }
        else if(type == "notimportantAndUrgent"){
            binding?.notImportantAndUrgent?.layoutManager = LinearLayoutManager(context)
            taskAdapternotimpurg = TaskAdapter({taskModel: TaskModel -> completeTask(taskModel)  },
                {taskModel: TaskModel -> editTask(taskModel)  })

            binding?.notImportantAndUrgent?.adapter = taskAdapternotimpurg
            filterTasks(type)
        }
    }

    private fun filterTasks(type: String) {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedValue = sharedPreferences.getString("email", "test")
        if(savedValue != "default value") {
            email = savedValue
        }

//        как было до реализации перетаскивания
//        val filteredList = taskViewModel?.tasks?.value?.filter { task ->
//            task.type == type && task.email == email && task.completed != "true"
//        }
//
//        taskAdapter?.setList(filteredList as List<TaskModel>)
//        taskAdapter?.notifyDataSetChanged()


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

//        recyclerView?.let {
//            it.layoutManager = LinearLayoutManager(context)
////            taskAdapter = TaskAdapter(
////                { taskModel: TaskModel -> completeTask(taskModel) },
////                { taskModel: TaskModel -> editTask(taskModel) }
////            )
//            it.adapter = taskAdapterin
//
//            // Attach the ItemTouchHelper to the correct RecyclerView
//            val callback = SimpleItemTouchHelperCallback(taskAdapterin!!)
//            val touchHelper = ItemTouchHelper(callback)
//            touchHelper.attachToRecyclerView(it)
//        }

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

//    override fun onItemMove(fromRecyclerView: RecyclerView,
//                            fromPosition: Int,
//                            toRecyclerView: RecyclerView,
//                            toPosition: Int) {
//        val fromAdapter = fromRecyclerView.adapter as? TaskAdapter
//        val toAdapter = toRecyclerView.adapter as? TaskAdapter
//        val task = fromAdapter?.getItem(fromPosition)
//
//        // Update the type of task based on the target RecyclerView
//        val newType = when (toRecyclerView) {
//            binding?.important -> "important"
//            binding?.urgent -> "urgent"
//            // Add cases for each RecyclerView
//            else -> return
//        }
//
//        task?.let {
//            it.type = newType
//            fromAdapter?.removeItem(fromPosition)
//            toAdapter?.insertTask(toPosition, it)
//
//            // Show loading panel
//            panelLoad = LoadingData()
//            panelLoad?.show((context as FragmentActivity).supportFragmentManager, "addTask")
//
//            // Update the database
//            //updateTaskInDatabase(it) {
//                // After saving to the database, hide loading panel and refresh lists
//
//
//            }
//
////            deleteTaskInDatabase(it){
////                panelLoad?.dismiss()
////            }
//        }
//    }

//    override fun updateTaskInDatabase(it: TaskModel, function: () -> Unit) {
//
//        taskViewModel?.startInsertModel(it)
//    }
//
//    override fun deleteTaskInDatabase(it: TaskModel, function: () -> Unit) {
//        taskViewModel?.deleteTask(it)
//    }

//    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onItemDropped(
//        initialRecyclerView: RecyclerView,
//        initialPosition: Int,
//        finalRecyclerView: RecyclerView,
//        finalPosition: Int
//    ) {
//        TODO("Not yet implemented")
//    }


}