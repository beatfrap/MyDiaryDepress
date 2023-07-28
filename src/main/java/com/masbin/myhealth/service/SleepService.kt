// SleepService.kt
package com.masbin.myhealth.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.concurrent.timerTask

class SleepService : Service() {
    private val random = Random()
    private lateinit var timer: Timer
    private lateinit var handler: Handler
    private var isServiceRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "SleepService started")
        handler = Handler()
        startSleepUpdates()

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
                handler.postDelayed(this, 5 * 60 * 1000) // Update setiap 5 menit
            }
        }
    }

    private fun updateAndSendSleepData() {
        val sleepValue = getSleepData(1, 8)
        Log.d(TAG, "Updating sleep data: $sleepValue")

        // Kirim data ke server
        val userId = 1 // Ganti dengan ID pengguna yang sesuai
        sendSleepData(userId, sleepValue)
    }

    private fun getSleepData(start: Int, end: Int): Int {
        return random.nextInt(end - start + 1) + start
    }

    private fun sendSleepData(userId: Int, sleepValue: Int) {
        val url = "https://beflask.as.r.appspot.com//post/sleep/$userId"
        val requestBody = FormBody.Builder()
            .add("sleep", sleepValue.toString())
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
                    Log.d(TAG, "Data sent successfully")
                } else {
                    Log.d(TAG, "Failed to send data")
                }
                response.close()
            }
        })
    }

    companion object {
        private const val TAG = "SleepService"
        const val ACTION_SLEEP_UPDATE = "com.masbin.myhealth.service.action.SLEEP_UPDATE"
    }
}
