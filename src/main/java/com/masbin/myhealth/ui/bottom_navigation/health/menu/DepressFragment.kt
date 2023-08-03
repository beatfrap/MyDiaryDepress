// DepressFragment.kt
package com.masbin.myhealth.ui.bottom_navigation.health.menu

import android.annotation.SuppressLint
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

class DepressFragment : Fragment() {
    private lateinit var depressionLevelTextView: TextView

    // Define the DepressionData class inside DepressFragment
    data class DepressionData(val level: Int, val userId: Int)

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_depress, container, false)
        depressionLevelTextView = view.findViewById(R.id.valueDepress)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            val userId = sharedPreferences.getInt("id", -1)
            if (userId != -1) {
                // Fetch depression level data from Flask server and display it in the TextView
                fetchDataFromServer(userId)
            } else {
                depressionLevelTextView.text = "0"
            }
        } else {
            depressionLevelTextView.text = "0"
        }
    }

    private fun fetchDataFromServer(userId: Int) {
        val client = OkHttpClient()
        // Sertakan userId sebagai parameter pada URL permintaan
        val request = Request.Builder()
            .url("https://beflask.as.r.appspot.com//get/depress/history?user_id=$userId") // Sesuaikan dengan endpoint Flask untuk data tingkat depresi
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
                    // Handle the server response and update the TextView with depression level data
                    // You need to parse the responseData according to the JSON format
                    // returned by the Flask server.
                    responseData?.let {
                        val depressionList = parseDepressionData(it)
                        updateUI(depressionList)
                    }
                }
            })
        }
    }


    private fun parseDepressionData(responseData: String): List<DepressionData> {
        val depressionList = mutableListOf<DepressionData>()
        try {
            val jsonArray = JSONArray(responseData)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val level = jsonObject.getInt("depress")
                val userId = jsonObject.getInt("user_id")
                depressionList.add(DepressionData(level, userId))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return depressionList
    }

    private fun updateUI(depressionList: List<DepressionData>) {
        // Check if the depressionList is not empty
        if (depressionList.isNotEmpty()) {
            // Get the latest data from the depressionList (the latest data is the last one in the list)
            val latestDepressionData = depressionList.last()

            // Get the depression level from the latest data
            val depressionLevel = latestDepressionData.level

            // Update the text in depressionLevelTextView with the depression level
            depressionLevelTextView.text = depressionLevel.toString()
        } else {
            // If there is no data, set the text in depressionLevelTextView to an empty message or as needed
            depressionLevelTextView.text = ""
        }
    }
}
