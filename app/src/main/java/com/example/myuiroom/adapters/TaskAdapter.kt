package com.example.myuiroom.adapters

import android.content.ClipData
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myuiroom.R
import com.example.myuiroom.databinding.TaskItemBinding
import com.example.myuiroom.models.TaskModel
import com.example.myuiroom.tabs.move.DragListener
import com.example.myuiroom.tabs.move.Listener
import kotlinx.coroutines.withContext

class TaskAdapter (private val listener: Listener, private val completeTask:(TaskModel)->Unit,
                   private val editTask:(TaskModel)->Unit, private val deleteTask: (TaskModel) -> Unit, private val isDeleteVisible: Boolean) : RecyclerView.Adapter<TaskAdapter.TaskHolder>(), View.OnTouchListener {

    private var taskList = ArrayList<TaskModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {

        val binding : TaskItemBinding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return TaskHolder(binding, isDeleteVisible)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    fun getItem(position: Int): TaskModel {
        return taskList[position]
    }

    fun removeItem(position: Int) {
        taskList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {

        val taskModel = taskList[position]
        holder.bind(taskModel, completeTask, editTask, deleteTask)
        holder.elementField.tag = position
        holder.elementField.setOnClickListener{
            editTask(taskModel) }


        holder.elementField.setOnLongClickListener {
            val data = ClipData.newPlainText("", "")
            val shadowBuilder = View.DragShadowBuilder(it)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.startDragAndDrop(data, shadowBuilder, it, 0)
            } else {
                it.startDrag(data, shadowBuilder, it, 0)
            }
            true
        }
        holder.elementField.setOnDragListener(DragListener(listener))

    }

    fun setList(task: List<TaskModel>) {
        taskList.clear()
        taskList.addAll(task)
    }

    class TaskHolder(val binding: TaskItemBinding, private val isDeleteVisible: Boolean) : RecyclerView.ViewHolder(binding.root) {

        fun bind(taskModel: TaskModel,
                 completeTask: (TaskModel) -> Unit,
                 editTask: (TaskModel) -> Unit,
                 deleteTask: (TaskModel) -> Unit
        ) {
            binding.nameTask.text = taskModel.name
            binding.deleteTask.visibility = if (isDeleteVisible) View.VISIBLE else View.GONE
            binding.completeTask.setOnClickListener({
                completeTask(taskModel) }, )
            binding.deleteTask.setOnClickListener({deleteTask(taskModel)})
            if(taskModel.completed == "true"){
                binding.completeTask.setImageResource(R.drawable.done_blue)
                val color = ContextCompat.getColor(binding.root.context, R.color.light_blue)
                binding.nameTask.setTextColor(color)
            }

            when (taskModel.category) {
                "family" -> binding.ibCategory.setImageResource(R.drawable.happy_house__16)
                "job" -> binding.ibCategory.setImageResource(R.drawable.job_seeker_16)
                "study" -> binding.ibCategory.setImageResource(R.drawable.table_16)
                "health" -> binding.ibCategory.setImageResource(R.drawable.medical_check_16)
                "everything" -> binding.ibCategory.setImageResource(R.drawable.testing_16)
                // Add more categories as needed
                else -> binding.ibCategory.setImageResource(R.drawable.testing_16) // default icon
            }



        }

        val elementField = binding.cardTask
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val data = ClipData.newPlainText("", "")
            val shadowBuilder = View.DragShadowBuilder(v)
            v.startDragAndDrop(data, shadowBuilder, v, 0)
            return true
        }
        return false
    }

    fun getList(): ArrayList<TaskModel> = taskList

    fun updateList(list: ArrayList<TaskModel>) {
        this.taskList = list
    }

    fun getDragInstance(): DragListener? {
        if (listener != null){
            return DragListener(listener)
        } else {

            return  null
        }
    }
}