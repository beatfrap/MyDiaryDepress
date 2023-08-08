package com.masbin.myhealth.service

// MasterService.kt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.masbin.myhealth.R

const val NOTIFICATION_CHANNEL_ID = "master_service_channel"
const val NOTIFICATION_ID = 1

class MasterService : Service() {

    override fun onCreate() {
        super.onCreate()

        // Start the other services
        startService(Intent(this, HeartService::class.java))
        startService(Intent(this, SleepService::class.java))
        startService(Intent(this, StressService::class.java))

        // Create and show the notification to keep the service running in the background
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Master Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//            .setContentTitle("Health")
            .setContentText("Connected")
            .setSmallIcon(R.drawable.icon_app)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        return notification
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
