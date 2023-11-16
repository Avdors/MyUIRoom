package com.example.myuiroom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.myuiroom.databinding.ActivityMainBinding
import com.example.myuiroom.notices.channelID
import com.example.myuiroom.tabs.Login
import com.example.myuiroom.tabs.TaskAll
import com.example.myuiroom.tabs.TaskForType

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        supportFragmentManager.beginTransaction().replace(R.id.content, Login()).commit()
        binding?.bottomNav?.selectedItemId = R.id.loginBottomNav

        binding?.bottomNav?.setOnItemSelectedListener { item ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotification()
            }
            when(item.itemId){
                R.id.loginBottomNav -> supportFragmentManager.beginTransaction().replace(R.id.content, Login()).commit()
                R.id.allList -> supportFragmentManager.beginTransaction().replace(R.id.content, TaskAll()).commit()
                R.id.taskItemBottomNav -> supportFragmentManager.beginTransaction().replace(R.id.content, TaskForType()).commit()
            }
            return@setOnItemSelectedListener true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification() {
        val name = "Notif Channel"
        val desc = "A description of the Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc

        // Set notification sound

        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
        channel.enableVibration(true)

        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)


    }
}