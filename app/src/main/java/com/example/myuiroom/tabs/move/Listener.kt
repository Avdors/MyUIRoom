package com.example.myuiroom.tabs.move

interface Listener {
    fun setEmptyListImportant(visibility: Boolean)
    fun setEmptyListNotImportant(visibility: Boolean)
    fun setEmptyListUrgent(visibility: Boolean)
    fun setEmptyListImportantUrgent(visibility: Boolean)
}