// SleepService.kt
package com.masbin.myhealth.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.masbin.myhealth.ui.signin.UserManager
//import com.masbin.myhealth.ui.signin.UserManager
import okhttp3.*
import java.io.IOException
import java.util.*

class SleepService : Service() {
    private val random = Random()
    private lateinit var timer: Timer
    private lateinit var handler: Handler
    private var isServiceRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "SleepService started")
        handler = Handler()
        startSleepUpdates()
        if (UserManager.isLoggedIn()) {
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val userId =
                UserManager.getUserId() // Mendapatkan ID pengguna yang sedang login dari sistem autentikasi
            sharedPreferences.edit().putInt("userId", userId).apply()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSleepUpdates()
        Log.d(TAG, "SleepService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startSleepUpdates() {
        isServiceRunning = true
        handler.post(updateRunnable)
    }

    private fun stopSleepUpdates() {
        isServiceRunning = false
        handler.removeCallbacks(updateRunnable)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isServiceRunning) {
                updateAndSendSleepData()
                handler.postDelayed(this,  8 * 60 * 60 * 1000) // Update setiap 5 menit
            }
        }
    }

    private fun updateAndSendSleepData() {
        val sleepValue = 6
        Log.d(TAG, "Updating sleep data: $sleepValue")

        if (UserManager.isLoggedIn()) {
            val userId = UserManager.getUserId()
            sendSleepData(userId, sleepValue)
        } else {
            Log.d(TAG, "User not logged in, cannot send sleep data")
        }
    }

    private fun getSleepData(start: Int): Int {
        return random.nextInt(start)
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
        private const val TAG = "SleepService"
    }
}
