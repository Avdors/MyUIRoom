package com.example.myuiroom

import android.content.Context
import android.content.SharedPreferences

class MyConstans  (context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

    var email: String?
        get() = prefs.getString("email", null)
        set(value) = prefs.edit().putString("email", value).apply()
}