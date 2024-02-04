package com.example.myuiroom.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "task_data_table")
class TaskModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id")
    var id : Int,

    @ColumnInfo(name = "task_name")
    var name : String,

    @ColumnInfo(name = "task_info")
    var info : String,

    @ColumnInfo(name = "task_email")
    var email : String,

    @ColumnInfo(name = "task_type")
    var type : String,

    @ColumnInfo(name = "task_dateStart")
    var dateStart : String,

    @ColumnInfo(name = "task_dateEnd")
    var dateEnd : String,

    @ColumnInfo(name = "task_completed")
    var completed : String,

    @ColumnInfo(name = "task_day")
    var day : Int,

    @ColumnInfo(name = "task_hourOfDay")
    var hourOfDay : Int,

    @ColumnInfo(name = "task_minute")
    var minute : Int,

    @ColumnInfo(name = "task_show_alert")
    var show_alert : String,

    @ColumnInfo(name = "task_category")
    var category : String
)