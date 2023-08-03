// StressFragment.kt
package com.masbin.myhealth.ui.bottom_navigation.health.menu

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.masbin.myhealth.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class StressFragment : Fragment() {
    private lateinit var stressValueTextView: TextView

    // Definisikan kelas StressData di dalam StressFragment
    data class StressData(val stressLevel: Int, val userId: Int)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stress, container, false)
        stressValueTextView = view.findViewById(R.id.valueStress)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            val userId = sharedPreferences.getInt("id", -1)
            if (userId != -1) {
                // Ambil data tingkat stres dari server Flask dan tampilkan di TextView
                fetchDataFromServer(userId)
            } else {
                stressValueTextView.text = "0"
            }
        } else {
            stressValueTextView.text = "0"
        }
    }

    private fun fetchDataFromServer(userId: Int) {
        val client = OkHttpClient()
        // Sertakan userId sebagai parameter pada URL permintaan
        val request = Request.Builder()
            .url("https://beflask.as.r.appspot.com//get/stress?user_id=$userId") // Sesuaikan dengan endpoint Flask untuk data tingkat stres
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
                    // Tangani respons dari server dan update TextView dengan data tingkat stres
                    // Anda harus parsing responseData yang sesuai dengan format JSON
                    // yang dikembalikan oleh server Flask.
                    responseData?.let {
                        val stressList = parseStressData(it)
                        updateUI(stressList)
                    }
                }
            })
        }
    }


    private fun parseStressData(responseData: String): List<StressData> {
        val stressList = mutableListOf<StressData>()
        try {
            val jsonArray = JSONArray(responseData)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val stressLevel = jsonObject.getInt("stress")
                val userId = jsonObject.getInt("user_id")
                stressList.add(StressData(stressLevel, userId))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stressList
    }

    private fun updateUI(stressList: List<StressData>) {
        // Periksa apakah daftar stressList tidak kosong
        if (stressList.isNotEmpty()) {
            // Ambil data terbaru dari daftar stressList (data terbaru adalah yang paling akhir dalam daftar)
            val latestStressData = stressList.last()

            // Ambil nilai stressLevel dari data terbaru
            val stressLevel = latestStressData.stressLevel

            // Update teks pada stressValueTextView dengan nilai stressLevel
            stressValueTextView.text = stressLevel.toString()
        } else {
            // Jika tidak ada data, atur teks pada stressValueTextView menjadi pesan kosong atau sesuai kebutuhan
            stressValueTextView.text = ""
        }
    }
}
