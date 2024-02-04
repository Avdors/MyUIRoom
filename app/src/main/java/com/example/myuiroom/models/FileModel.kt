package com.example.myuiroom.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "file_data_table",
    foreignKeys = [
        ForeignKey(
            entity = TaskModel::class,
            parentColumns = ["task_id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FileModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "file_id")
    val fileId: Int,

    @ColumnInfo(name = "task_id")
    val taskId: Int,  // Foreign key

    @ColumnInfo(name = "file_path")
    val filePath: String
)