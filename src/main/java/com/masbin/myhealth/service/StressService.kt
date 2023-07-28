// StressService.kt
package com.masbin.myhealth.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.concurrent.timerTask

class StressService : Service() {
    private val random = Random()
    private lateinit var timer: Timer
    private var isFirstRun = true // Tambahkan boolean ini untuk melacak status service

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "StressService started")

        if (isFirstRun) {
            updateAndSendStressData()
            isFirstRun = false
        }

        timer = Timer()
        timer.schedule(timerTask {
            updateAndSendStressData()
        }, 5 * 60 * 1000, 5 * 60 * 1000) // Setiap 5 menit setelah 5 menit pertama

        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        Log.d(TAG, "StressService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun updateAndSendStressData() {
        val stressValue = getStressData(40, 60)
        Log.d(TAG, "Updating stress data: $stressValue")

        // Kirim data ke server
        val userId = 1 // Ganti dengan ID pengguna yang sesuai
        sendStressData(userId, stressValue)
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
                    Log.d(TAG, "Data sent successfully")
                } else {
                    Log.d(TAG, "Failed to send data")
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
