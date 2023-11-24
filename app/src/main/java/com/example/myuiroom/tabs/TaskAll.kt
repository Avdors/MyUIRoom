package com.example.myuiroom.tabs

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myuiroom.R
import com.example.myuiroom.adapters.TaskAdapter
import com.example.myuiroom.data.Database
import com.example.myuiroom.databinding.TaskAllBinding
import com.example.myuiroom.models.TaskModel
import com.example.myuiroom.repositories.TaskRepository
import com.example.myuiroom.tabs.move.Listener
import com.example.myuiroom.viewModels.TaskFactory
import com.example.myuiroom.viewModels.TaskViewModel
import java.time.LocalDate


class TaskAll : Fragment(), View.OnClickListener, Listener {

    private var binding: TaskAllBinding? = null
    private var taskRepository: TaskRepository? = null
    private var taskViewModel: TaskViewModel? = null
    private var taskFactory: TaskFactory? = null
    private var taskAdapter: TaskAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TaskAllBinding.inflate(inflater, container, false)

        val taskDao = Database.getInstance((context as FragmentActivity).application).taskDao
        taskRepository = TaskRepository(taskDao)
        taskFactory = TaskFactory(taskRepository!!)
        taskViewModel = ViewModelProvider(this, taskFactory!!).get(TaskViewModel::class.java)
        // Inflate the layout for this fragment
        initRecyclerProducts()
        loadTask()
        binding?.buttonAddTask?.setOnClickListener(this)


        return binding?.root
    }

    private fun loadTask() {
        taskViewModel?.tasks?.observe(viewLifecycleOwner, Observer {
            taskAdapter?.setList(it)
            taskAdapter?.notifyDataSetChanged()
        })
    }

    private fun initRecyclerProducts() {
        binding?.recyclerTask?.layoutManager = LinearLayoutManager(context)
        taskAdapter = TaskAdapter(this, {taskModel: TaskModel ->  completeTask(taskModel)  },
            {taskModel: TaskModel -> editTask(taskModel)  })
        binding?.recyclerTask?.adapter = taskAdapter
    }

    private fun completeTask(taskModel: TaskModel) {

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
        parameters.putString("nameForm", "TaskAll")
        parameters.putString("taskAction", "Edit")
        panelEditTask.arguments = parameters

        panelEditTask.show((context as FragmentActivity).supportFragmentManager, "editTask")
    }


    override fun onClick(view: View) {
        val currentDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val dateString = currentDate.toString()

        when(view.id){
            R.id.buttonAddTask -> {
                val panelEditTask = PanelEditTask()
                val parameters = Bundle()
                parameters.putString("dateStart", dateString)
                parameters.putString("completed", "")
                parameters.putString("nameForm", "TaskAll")
                parameters.putString("taskAction", "Add")
                panelEditTask.arguments = parameters
                panelEditTask.show((context as FragmentActivity).supportFragmentManager, "addTask")


            }

        }
    }

    override fun setEmptyListImportant(visibility: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setEmptyListNotImportant(visibility: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setEmptyListUrgent(visibility: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setEmptyListImportantUrgent(visibility: Boolean) {
        TODO("Not yet implemented")
    }

    override fun editTaskType(taskModel: TaskModel, type: String) {
        TODO("Not yet implemented")
    }
}