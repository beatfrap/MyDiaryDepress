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

class StressFragment : Fragment() {
    private val random = Random()
    private lateinit var timer: Timer

    // TextView untuk menampilkan angka acak
    private lateinit var valueStressTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_stress, container, false)
        valueStressTextView = view.findViewById(R.id.valueStress)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateStressData()

        timer = Timer()
        timer.schedule(timerTask {
            activity?.runOnUiThread {
                updateStressData()

                // Ambil ID pengguna setelah login
                val userId = getLoggedInUserId()

                // Kirim data ke endpoint Flask
                val stress = valueStressTextView.text.toString()
                val classification = "normal"

                sendStressData(userId, stress, classification)
            }
        }, 0, 5 * 60 * 1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer.cancel()
    }

    private fun updateStressData() {
        val randomNumber = getStressData(40, 60)
        valueStressTextView.text = randomNumber.toString()
    }

    private fun getStressData(start: Int, end: Int): Int {
        return random.nextInt(end - start + 1) + start
    }

    private fun getLoggedInUserId(): Int {
        // Implementasikan logika untuk mendapatkan ID pengguna yang sedang login
        // Anda dapat menggunakan metode dari library atau framework yang digunakan dalam proyek Anda

        return 123 // Contoh ID pengguna statis
    }

    private fun sendStressData(userId: Int, stress: String, classification: String) {
        val url = "https://beflask.as.r.appspot.com//post/stress/$userId"
        val requestBody = FormBody.Builder()
            .add("stress", stress)
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
