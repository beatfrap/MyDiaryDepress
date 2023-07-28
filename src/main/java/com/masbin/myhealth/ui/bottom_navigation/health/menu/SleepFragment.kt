// SleepFragment.kt
package com.masbin.myhealth.ui.bottom_navigation.health.menu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.masbin.myhealth.R
import com.masbin.myhealth.service.SleepService

class SleepFragment : Fragment() {

    private lateinit var valueSleepTextView: TextView

    private val sleepUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val sleepValue = intent.getIntExtra("sleepValue", 0)
            updateSleepValue(sleepValue)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)
        valueSleepTextView = view.findViewById(R.id.valueSleep)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Daftarkan receiver untuk menerima update data tidur dari SleepService
        val filter = IntentFilter(SleepService.ACTION_SLEEP_UPDATE)
        requireActivity().registerReceiver(sleepUpdateReceiver, filter)

        // Update data tidur untuk pertama kali
        updateSleepValue(6)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hapus receiver saat fragment di-destroy
        requireActivity().unregisterReceiver(sleepUpdateReceiver)
    }

    private fun updateSleepValue(sleepValue: Int) {
        valueSleepTextView.text = sleepValue.toString()
    }
}
