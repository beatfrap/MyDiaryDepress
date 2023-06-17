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
import kotlin.concurrent.timer

class HeartFragment : Fragment() {
    private val random = Random()
    private lateinit var timer: Timer

    // TextView untuk menampilkan angka acak
    private lateinit var heartValueTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_heart, container, false)
        heartValueTextView = view.findViewById(R.id.heartValue)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateHeartRateData()

        timer = timer(period = 5 * 60 * 1000) {
            updateHeartRateData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer.cancel()
    }

    private fun updateHeartRateData() {
        val randomNumber = getHeartRateData(80, 110)
        heartValueTextView.text = randomNumber.toString()

        // Kirim data ke endpoint Flask
        val userId = 3 // Ganti dengan ID pengguna yang sesuai
        val heartrate = randomNumber.toString()// Ganti dengan klasifikasi yang sesuai

        sendHeartrate(userId, heartrate)
    }

    private fun getHeartRateData(start: Int, end: Int): Int {
        return random.nextInt(end - start + 1) + start
    }

    private fun sendHeartrate(userId: Int, heartrate: String) {
        val url = "https://beflask.as.r.appspot.com//post/heartrate/$userId"
        val requestBody = FormBody.Builder()
            .add("heartrate", heartrate)
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
