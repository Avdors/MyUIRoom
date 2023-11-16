package com.example.myuiroom.notices

import java.io.Serializable
import java.sql.Date

data class TaskInfo (var id : Int,
                     var description : String,
                     var priority : Int,
                     var status : Boolean,
                     var category: String
): Serializable