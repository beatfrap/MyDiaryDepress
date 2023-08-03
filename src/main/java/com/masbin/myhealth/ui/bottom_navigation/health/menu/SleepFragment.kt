// SleepFragment.kt
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

class SleepFragment : Fragment() {
    private lateinit var sleepDurationTextView: TextView

    // Define the SleepData class inside SleepFragment
    data class SleepData(val duration: Int, val userId: Int)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)
        sleepDurationTextView = view.findViewById(R.id.valueSleep)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ganti userId dengan ID user yang sesuai, misalnya 1
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)
        if (userId != -1) {
            // Ambil data durasi tidur dari server Flask dan tampilkan di TextView
            fetchDataFromServer(userId)
        } else {
            sleepDurationTextView.text = "0"
        }
    }

    private fun fetchDataFromServer(userId: Int) {
        val client = OkHttpClient()
        // Sertakan userId sebagai parameter pada URL permintaan
        val request = Request.Builder()
            .url("https://beflask.as.r.appspot.com//get/sleep?user_id=$userId") // Sesuaikan dengan endpoint Flask untuk data durasi tidur
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle errors when fetching data from the server
                    // For example, display an error message or log for debugging
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    // Handle the server response and update the TextView with sleep duration data
                    // You need to parse the responseData according to the JSON format
                    // returned by the Flask server.
                    responseData?.let {
                        val sleepList = parseSleepData(it)
                        updateUI(sleepList)
                    }
                }
            })
        }
    }


    private fun parseSleepData(responseData: String): List<SleepData> {
        val sleepList = mutableListOf<SleepData>()
        try {
            val jsonArray = JSONArray(responseData)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val duration = jsonObject.getInt("sleep")
                val userId = jsonObject.getInt("user_id")
                sleepList.add(SleepData(duration, userId))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sleepList
    }

    private fun updateUI(sleepList: List<SleepData>) {
        // Check if the sleepList is not empty
        if (sleepList.isNotEmpty()) {
            // Get the latest data from the sleepList (the latest data is the last one in the list)
            val latestSleepData = sleepList.last()

            // Get the sleep duration from the latest data
            val sleepDuration = latestSleepData.duration

            // Update the text in sleepDurationTextView with the sleep duration
            sleepDurationTextView.text = sleepDuration.toString()
        } else {
            // If there is no data, set the text in sleepDurationTextView to an empty message or as needed
            sleepDurationTextView.text = "-1"
        }
    }
}
