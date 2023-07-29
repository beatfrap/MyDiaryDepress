// StressService.kt
package com.masbin.myhealth.service

import android.app.Notification
import android.app.PendingIntent
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
import kotlin.concurrent.timerTask

class StressService : Service() {
    private val random = Random()
    private lateinit var timer: Timer
    private lateinit var handler: Handler
    private var isServiceRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "StressService started")
        handler = Handler()
        startStressUpdates()

        return START_STICKY
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        stopStressUpdates()
//        Log.d(TAG, "StressService destroyed")
//    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startStressUpdates() {
        isServiceRunning = true
        handler.post(updateRunnable)
    }

    private fun stopStressUpdates() {
        isServiceRunning = false
        handler.removeCallbacks(updateRunnable)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isServiceRunning) {
                updateAndSendStressData()
                handler.postDelayed(this, 5 * 60 * 1000) // Update setiap 5 menit
            }
        }
    }

    private fun updateAndSendStressData() {
        val stressValue = getStressData(40, 70)
        Log.d(TAG, "Updating stress data: $stressValue")

        if (UserManager.isLoggedIn()) {
            val userId = UserManager.getUserId()
            sendStressData(userId, stressValue)
        } else {
            Log.d(TAG, "User not logged in, cannot send stress data")
        }
    }

    private fun getStressData(start: Int, end: Int): Int {
        return random.nextInt(end - start + 1) + start
    }

    private fun sendStressData(userId: Int, stress: Int) {
        val url = "https://beflask.as.r.appspot.com/post/stress/$userId"
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
                    Log.d(TAG, "Data Stress sent successfully")
                } else {
                    Log.d(TAG, "Failed to send data Stress")
                }
                response.close()
            }
        })
    }

    companion object {
        private const val TAG = "StressService"
        const val ACTION_STRESS_UPDATE = "com.masbin.myhealth.service.action.STRESS_UPDATE"
        const val EXTRA_STRESS_VALUE = "com.masbin.myhealth.service.extra.STRESS_VALUE"
    }
}
