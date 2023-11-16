package com.example.myuiroom.tabs.move

import com.example.myuiroom.models.TaskModel

interface Listener {
    fun setEmptyListImportant(visibility: Boolean)
    fun setEmptyListNotImportant(visibility: Boolean)
    fun setEmptyListUrgent(visibility: Boolean)
    fun setEmptyListImportantUrgent(visibility: Boolean)
    fun editTaskType(taskModel: TaskModel, type: String)
}