package com.masbin.myhealth.ui.bottom_navigation.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.masbin.myhealth.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject

class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    data class DepressionData(val level: Int, val classification:String, val userId: Int)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Menambahkan onClickListener pada tombol btnConnect
        binding.btnConnect.setOnClickListener {
            val intent = Intent(activity, ConnectBleActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check login status
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            val userId = sharedPreferences.getInt("id", -1)
            if (userId != -1) {
                // Ambil data tingkat stres dari server Flask dan tampilkan di TextView
                fetchHistoryDataFromServer(userId)
            } else {
                // Pastikan _binding telah diinisialisasi sebelum menggunakannya
                _binding?.valueDepressReal?.text = "0"
                _binding?.tvStatusDepress?.text = "Normal"
            }
        } else {
            // Pastikan _binding telah diinisialisasi sebelum menggunakannya
            _binding?.valueDepressReal?.text = "0"
            _binding?.tvStatusDepress?.text = "Normal"
        }
    }



    private fun fetchHistoryDataFromServer(userId: Int) {
        val client = OkHttpClient()
        // Sertakan userId sebagai parameter pada URL permintaan
        val request = Request.Builder()
            .url("https://beflask.as.r.appspot.com//get/depress/history/week?user_id=$userId") // Sesuaikan dengan endpoint Flask untuk data histori depresi mingguan
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
                    // Handle the server response and update the TextView with depression history data
                    // You need to parse the responseData according to the JSON format
                    // returned by the Flask server.
                    responseData?.let {
                        val depressionHistoryList = parseDepressionHistoryData(it)
                        updateUI(depressionHistoryList)
                    }
                }
            })
        }
    }

    private fun parseDepressionHistoryData(responseData: String): List<DepressionData> {
        val depressionHistoryList = mutableListOf<DepressionData>()
        try {
            val jsonArray = JSONArray(responseData)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val level = jsonObject.getInt("depress")
                val classification = jsonObject.getString("classification")
                val userId = jsonObject.getInt("user_id")
                depressionHistoryList.add(DepressionData(level,classification, userId))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return depressionHistoryList
    }

    private fun updateUI(depressionHistoryList: List<DepressionData>) {
        // Display the weekly history of depression levels in the TextView
        // Format the data as needed to show the history over the week
        // For example, you can use a StringBuilder to construct the text to display.

        if (depressionHistoryList.isNotEmpty()) {
            // Get the latest data from the depressionList (the latest data is the last one in the list)
            val latestDepressionData = depressionHistoryList.last()

            // Get the depression level from the latest data
            val depressionLevel = latestDepressionData.level
            val depressionClassification = latestDepressionData.classification

            activity?.runOnUiThread {
                // Find the TextView by its ID in the fragment_home.xml and update its text with the depression history data
                binding.valueDepressReal.text = depressionLevel.toString()
                binding.tvStatusDepress.text = depressionClassification
            }
        } else {
            // If there is no data, you can handle it here (e.g., show a message)
            Log.d("HomeFragment", "Depression history list is empty")
        }
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
}
