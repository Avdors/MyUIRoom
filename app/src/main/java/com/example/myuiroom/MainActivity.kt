package com.example.myuiroom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.myuiroom.databinding.ActivityMainBinding
import com.example.myuiroom.servic.FileCleanupService
import com.example.myuiroom.tabs.Login
import com.example.myuiroom.tabs.TaskAll
import com.example.myuiroom.tabs.TaskForType

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // запускаю службу отчистки не используемых файлов
        val intent = Intent(this, FileCleanupService::class.java)
        startService(intent)

        if (intent.hasExtra("OpenFragment")) {
            val fragmentToOpen = intent.getStringExtra("OpenFragment")
            val idTask = intent.getStringExtra("idTask")
            if (fragmentToOpen == "TaskForType") {
                openTaskForType(idTask.toString())
            }
            else
            {
                supportFragmentManager.beginTransaction().replace(R.id.content, Login()).commit()
                binding?.bottomNav?.selectedItemId = R.id.loginBottomNav
            }

            } else {
            supportFragmentManager.beginTransaction().replace(R.id.content, Login()).commit()
            binding?.bottomNav?.selectedItemId = R.id.loginBottomNav
             }




        binding?.bottomNav?.setOnItemSelectedListener { item ->

            when(item.itemId){
                R.id.loginBottomNav -> supportFragmentManager.beginTransaction().replace(R.id.content, Login()).commit()
                R.id.allList -> supportFragmentManager.beginTransaction().replace(R.id.content, TaskAll()).commit()
                R.id.taskItemBottomNav -> supportFragmentManager.beginTransaction().replace(R.id.content, TaskForType()).commit()
            }
            return@setOnItemSelectedListener true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotification()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification() {
        //val name = "Notif Channel"
        val desc = "A description of the Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, nameChannel, importance)
        channel.description = desc

        // Set notification sound

        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
        channel.enableVibration(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d("MyLog", "Main activity  notificationManager $notificationManager")

    }

    // open fragment TaskForType if Value filled OpenFragment
    private fun openTaskForType(idTask: String) {
        val fragment = TaskForType().apply {
            arguments = Bundle().apply {
                putString("idTask", idTask)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, fragment)
            .commit()
        binding?.bottomNav?.selectedItemId = R.id.taskItemBottomNav
    }
}