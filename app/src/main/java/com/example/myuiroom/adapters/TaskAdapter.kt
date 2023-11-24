package com.example.myuiroom.adapters

import android.content.ClipData
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.myuiroom.R
import com.example.myuiroom.databinding.TaskItemBinding
import com.example.myuiroom.models.TaskModel
import com.example.myuiroom.tabs.move.DragListener
import com.example.myuiroom.tabs.move.Listener
import java.text.FieldPosition

class TaskAdapter (private val listener: Listener, private val completeTask:(TaskModel)->Unit,
                   private val editTask:(TaskModel)->Unit) : RecyclerView.Adapter<TaskAdapter.TaskHolder>(), View.OnTouchListener {

    private var taskList = ArrayList<TaskModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {

        val binding : TaskItemBinding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskHolder(binding)
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
      //  holder.bind(taskList[position], completeTask, editTask)
        val taskModel = taskList[position]
        holder.bind(taskModel, completeTask, editTask)
        holder.elementField.tag = position
        holder.elementField.setOnClickListener{
            editTask(taskModel) }
       // holder.elementField.setOnTouchListener(this)
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

    class TaskHolder(val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(taskModel: TaskModel,
                 completeTask: (TaskModel) -> Unit,
                 editTask: (TaskModel) -> Unit
        ) {
            binding.nameTask.text = taskModel.name
            binding.completeTask.setOnClickListener({
                completeTask(taskModel) })
            if(taskModel.completed == "true"){
                binding.completeTask.setImageResource(R.drawable.done_all)
            }
//            binding.editTask.setOnClickListener({
//                editTask(taskModel) })
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
            Log.e("ListAdapter", "Listener wasn't initialized!")
            return  null
        }
    }
}