package com.masbin.myhealth.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.masbin.myhealth.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Random
import java.util.Timer

class SmartbandService : Service(){
    private val random = Random()
    private lateinit var timer: Timer
    private lateinit var handler: Handler
    private var isServiceRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(HR, "Smartband Service started")
        handler = Handler()
        // Mengambil userId dari SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)

        if (userId == -1) {
            Log.d(HR, "User not logged in, cannot send heart rate data")
            // Pengguna belum login, lakukan sesuatu (misalnya, tampilkan pesan atau arahkan ke halaman login)
        } else {
            SmartbandStartHeartRateUpdates()
            SmartbandStartStressUpdates()
            SmartbandStartSleepUpdates()
            startForegroundService()
        }

        return START_STICKY
    }
    private fun startForegroundService() {
        val channelId = "SmartbandChannelId"
        val channelName = "Smartband Channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create the notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Smartband Service")
            .setContentText("Smartband Service is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        // Start the service as a foreground service with the notification
        startForeground(1, notification)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun SmartbandStartHeartRateUpdates() {
        isServiceRunning = true
        handler.post(updateRunnableHeartRate)
    }

    private fun SmartbandstopHeartRateUpdates() {
        isServiceRunning = false
        handler.removeCallbacks(updateRunnableHeartRate)
    }

    private val updateRunnableHeartRate = object : Runnable {
        override fun run() {
            if (isServiceRunning) {
                updateAndSendHeartRateData()
                handler.postDelayed(this, 5 * 60 * 1000) // Update setiap 5 menit
            }
        }
    }

    private fun updateAndSendHeartRateData() {
        val heartRateValue = getHeartRateData(40, 130)
        Log.d(HR, "Updating heart rate data: $heartRateValue")

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)
        if (userId != -1) {
            sendHeartRateData(userId, heartRateValue)
        } else {
            Log.d(HR, "User not logged in, cannot send heart rate data")
        }
    }

    private fun getHeartRateData(start: Int, end: Int): Int {
        return random.nextInt(end - start + 1) + start
    }

    private fun sendHeartRateData(userId: Int, heartRate: Int) {
        val url = "https://beflask.as.r.appspot.com//post/heartrate/$userId"
        val requestBody = FormBody.Builder()
            .add("heartrate", heartRate.toString())
            .build()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(HR, "Data Heart Rate sent successfully")
                } else {
                    Log.d(HR, "Failed to send data Heart Rate")
                }
                response.close()
            }
        })
    }

    private fun SmartbandStartStressUpdates() {
        isServiceRunning = true
        handler.post(updateRunnableStrress)
    }

    private fun stopStressUpdates() {
        isServiceRunning = false
        handler.removeCallbacks(updateRunnableStrress)
    }

    private val updateRunnableStrress = object : Runnable {
        override fun run() {
            if (isServiceRunning) {
                updateAndSendStressData()
                handler.postDelayed(this, 5 * 60 * 1000) // Update setiap 5 menit
            }
        }
    }

    private fun updateAndSendStressData() {
        val stressValue = getStressData(40, 70)
        Log.d(STRESS, "Updating stress data: $stressValue")

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)

        if (userId != -1) {
            sendStressData(userId, stressValue)
        } else {
            Log.d(STRESS, "User not logged in, cannot send stress data")
        }

        val broadcastIntent = Intent(SmartbandService.ACTION_STRESS_UPDATE)
            .putExtra(SmartbandService.EXTRA_STRESS_VALUE, stressValue)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
    }

    private fun getStressData(start: Int, end: Int): Int {
        return random.nextInt(end - start + 1) + start
    }

    private fun sendStressData(userId: Int, stress: Int) {
        val url = "https://beflask.as.r.appspot.com//post/stress/$userId"
        val requestBody = FormBody.Builder()
            .add("stress", stress.toString())
            .build()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(STRESS, "Data Stress sent successfully")
                } else {
                    Log.d(STRESS, "Failed to send data Stress")
                }
                response.close()
            }
        })
    }

    private fun SmartbandStartSleepUpdates() {
        isServiceRunning = true
        handler.post(updateRunnableSleep)
    }

    private fun stopSleepUpdates() {
        isServiceRunning = false
        handler.removeCallbacks(updateRunnableSleep)
    }

    private val updateRunnableSleep = object : Runnable {
        override fun run() {
            if (isServiceRunning) {
                updateAndSendSleepData()
                handler.postDelayed(this, 8 * 60 * 60 * 1000) // Update setiap 8 jam
            }
        }
    }

    private fun updateAndSendSleepData() {
        val sleepValue = getSleepData(6)
        Log.d(TAG, "Updating sleep data: $sleepValue")

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)

        if (userId != -1) {
            sendSleepData(userId, sleepValue)
        } else {
            Log.d(TAG, "User not logged in, cannot send sleep data")
        }
    }

    private fun getSleepData(start: Int): Int {
        return random.nextInt(start) // Menggunakan nilai acak antara 0 hingga start-1
    }

    private fun sendSleepData(userId: Int, sleep: Int) {
        val url = "https://beflask.as.r.appspot.com//post/sleep/$userId"
        val requestBody = FormBody.Builder()
            .add("sleep", sleep.toString())
            .build()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Data Sleep sent successfully")
                } else {
                    Log.d(TAG, "Failed to send data Sleep")
                }
                response.close()
            }
        })
    }


    companion object {
        const val HR = "HeartService"
        const val STRESS = "StressService"
        private const val TAG = "SleepService"
        const val ACTION_HEART_RATE_UPDATE = "com.masbin.myhealth.service.action.HEART_RATE_UPDATE"
        const val ACTION_STRESS_UPDATE = "com.masbin.myhealth.service.action.STRESS_UPDATE"
        const val EXTRA_STRESS_VALUE = "com.masbin.myhealth.service.extra.STRESS_VALUE"
    }

}