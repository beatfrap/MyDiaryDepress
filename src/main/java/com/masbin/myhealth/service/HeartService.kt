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

class HeartService : Service() {
    private val random = Random()
    private lateinit var timer: Timer
    private lateinit var handler: Handler
    private var isServiceRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "HeartService started")
        handler = Handler()
        // Mengambil userId dari SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)

        if (userId == -1) {
            Log.d(TAG, "User not logged in, cannot send heart rate data")
            // Pengguna belum login, lakukan sesuatu (misalnya, tampilkan pesan atau arahkan ke halaman login)
        } else {
            startHeartRateUpdates()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startHeartRateUpdates() {
        isServiceRunning = true
        handler.post(updateRunnable)
    }

    private fun stopHeartRateUpdates() {
        isServiceRunning = false
        handler.removeCallbacks(updateRunnable)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isServiceRunning) {
                updateAndSendHeartRateData()
                handler.postDelayed(this, 5 * 60 * 1000) // Update setiap 5 menit
            }
        }
    }

    private fun updateAndSendHeartRateData() {
        val heartRateValue = getHeartRateData(40, 130)
        Log.d(TAG, "Updating heart rate data: $heartRateValue")

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)
        if (userId != -1) {
            sendHeartRateData(userId, heartRateValue)
        } else {
            Log.d(TAG, "User not logged in, cannot send heart rate data")
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
                    Log.d(TAG, "Data Heart Rate sent successfully")
                } else {
                    Log.d(TAG, "Failed to send data Heart Rate")
                }
                response.close()
            }
        })
    }

    companion object {
        private const val TAG = "HeartService"
        const val ACTION_HEART_RATE_UPDATE = "com.masbin.myhealth.service.action.HEART_RATE_UPDATE"
    }
}
