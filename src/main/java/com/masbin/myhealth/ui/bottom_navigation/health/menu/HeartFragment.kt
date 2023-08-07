// HeartFragment.kt
package com.masbin.myhealth.ui.bottom_navigation.health.menu

import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.masbin.myhealth.R
import com.masbin.myhealth.service.HeartService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class HeartFragment : Fragment() {
    private lateinit var heartValueTextView: TextView

    // Definisikan kelas HeartData di dalam HeartFragment
    data class HeartData(val heartRate: Int, val userId: Int)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_heart, container, false)
        heartValueTextView = view.findViewById(R.id.heartValue)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            val userId = sharedPreferences.getInt("id", -1)
            if (userId != -1) {
                // Ambil data detak jantung dari server Flask dan tampilkan di TextView
                fetchDataFromServer(userId)
            } else {
                heartValueTextView.text = "0"
            }
        } else {
            heartValueTextView.text = "0"
        }

    }

    private fun fetchDataFromServer(userId: Int) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://diary-depression.as.r.appspot.com//get/heart?user_id=$userId") // Sesuaikan dengan endpoint Flask untuk data detak jantung dan sertakan user_id dalam URL
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Tangani jika ada kesalahan saat mengambil data dari server
                    // Misalnya, tampilkan pesan kesalahan atau log untuk debugging
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    // Tangani respons dari server dan update TextView dengan data detak jantung
                    // Anda harus parsing responseData yang sesuai dengan format JSON
                    // yang dikembalikan oleh server Flask.
                    responseData?.let {
                        val heartList = parseHeartData(it)
                        updateUI(heartList)
                    }
                }
            })
        }
    }

    private fun parseHeartData(responseData: String): List<HeartData> {
        val heartList = mutableListOf<HeartData>()
        try {
            val jsonArray = JSONArray(responseData)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val heartRate = jsonObject.getInt("heart")
                val userId = jsonObject.getInt("user_id")
                heartList.add(HeartData(heartRate, userId))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return heartList
    }

    private fun updateUI(heartList: List<HeartData>) {
        // Periksa apakah daftar heartList tidak kosong
        if (heartList.isNotEmpty()) {
            // Ambil data terbaru dari daftar heartList (data terbaru adalah yang paling akhir dalam daftar)
            val latestHeartData = heartList.last()

            // Ambil nilai heartRate dari data terbaru
            val heartRate = latestHeartData.heartRate

            // Update teks pada heartValueTextView dengan nilai heartRate
            heartValueTextView.text = heartRate.toString()
        } else {
            // Jika tidak ada data, atur teks pada heartValueTextView menjadi pesan kosong atau sesuai kebutuhan
            heartValueTextView.text = ""
        }
    }

}
