package com.masbin.myhealth.ui.bottom_navigation.health.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.masbin.myhealth.R
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.concurrent.timerTask

class SleepFragment : Fragment() {
    private val random = Random()
    private lateinit var timer: TimerTask

    // TextView untuk menampilkan angka acak
    private lateinit var valueSleepTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)
        valueSleepTextView = view.findViewById(R.id.valueSleep)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mendapatkan waktu saat ini
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)

        // Menghitung waktu hingga jam 7 pagi berikutnya
        val next7AM = Calendar.getInstance()
        next7AM.set(Calendar.HOUR_OF_DAY, 7)
        next7AM.set(Calendar.MINUTE, 0)
        next7AM.set(Calendar.SECOND, 0)
        next7AM.add(Calendar.DAY_OF_YEAR, if (currentHour >= 7) 1 else 0)

        val delay = next7AM.timeInMillis - currentTime.timeInMillis

        // Menampilkan angka acak pertama kali dengan nilai awal 6
        valueSleepTextView.text = "6"

        // Mengatur timer untuk menjalankan updateSleepData pada jam 7 pagi berikutnya
        timer = timerTask {
            activity?.runOnUiThread {
                updateSleepData()

                // Ambil ID pengguna setelah login
                val userId = getLoggedInUserId()

                // Kirim data ke endpoint Flask
                val sleep = valueSleepTextView.text.toString()
                val classification = "good"

                sendSleepData(userId, sleep, classification)
            }
        }

        // Menjalankan timer dengan delay
        Timer().schedule(timer, delay, 24 * 60 * 60 * 1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer.cancel()
    }

    private fun updateSleepData() {
        val randomNumber = getSleepData(1, 8)
        valueSleepTextView.text = randomNumber.toString()
    }

    private fun getSleepData(start: Int, end: Int): Int {
        return random.nextInt(end - start + 1) + start
    }

    private fun getLoggedInUserId(): Int {
        // Implementasikan logika untuk mendapatkan ID pengguna yang sedang login
        // Anda dapat menggunakan metode dari library atau framework yang digunakan dalam proyek Anda

        return 123 // Contoh ID pengguna statis
    }

    private fun sendSleepData(userId: Int, sleep: String, classification: String) {
        val url = "https://beflask.as.r.appspot.com//post/sleep/$userId"
        val requestBody = FormBody.Builder()
            .add("sleep", sleep)
            .add("classification", classification)
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
                    println("Data sent successfully")
                } else {
                    println("Failed to send data")
                }
                response.close()
            }
        })
    }
}
