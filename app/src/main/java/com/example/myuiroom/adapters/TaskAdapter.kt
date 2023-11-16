package com.example.myuiroom.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myuiroom.databinding.TaskItemBinding
import com.example.myuiroom.models.TaskModel
import java.text.FieldPosition

class TaskAdapter (private val completeTask:(TaskModel)->Unit,
                   private val editTask:(TaskModel)->Unit) : RecyclerView.Adapter<TaskAdapter.TaskHolder>() {

    private val taskList = ArrayList<TaskModel>()

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
        holder.bind(taskList[position], completeTask, editTask)
    }

    fun setList(task: List<TaskModel>) {
        taskList.clear()
        taskList.addAll(task)
    }

    // AVD здесь добавляю блок для реализации изменения позиции элемента списка
    // путем перетаскивания

    fun moveItem(fromPosition: Int, toPosition: Int){
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                taskList.swap(i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                taskList.swap(i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    // Расширение добавляющее функцию свап, для смены индекса элемента
    private fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
        val tmp = this[index1] // 'this' corresponds to the list
        this[index1] = this[index2]
        this[index2] = tmp
    }
    // метод для вставки новой задачи в список
    fun insertTask(position: Int, taskModel: TaskModel) {
        taskList.add(position, taskModel)
        notifyItemInserted(position)
    }
    // меняем тип задачи при перетаскивании из разных блоков
    fun changeTaskType(position: Int, newType: String) {
        val task = taskList[position]
        task.type = newType // Assuming TaskModel has a type property
        notifyItemChanged(position)
    }
    // заканчивается блок для блок для реализации изменения позиции элемента списка

    class TaskHolder(val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(taskModel: TaskModel,
                 completeTask: (TaskModel) -> Unit,
                 editTask: (TaskModel) -> Unit
        ) {
            binding.nameTask.text = taskModel.name
            binding.completeTask.setOnClickListener({
                completeTask(taskModel) })
            binding.editTask.setOnClickListener({
                editTask(taskModel) })
        }
    }
}