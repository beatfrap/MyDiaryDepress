// StressFragment.kt
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
import com.masbin.myhealth.service.StressService

class StressFragment : Fragment() {
    private lateinit var valueStressTextView: TextView
    private val stressUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == StressService.ACTION_STRESS_UPDATE) {
                val stressValue = intent.getIntExtra(StressService.EXTRA_STRESS_VALUE, 0)
                updateStressTextView(stressValue)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stress, container, false)
        valueStressTextView = view.findViewById(R.id.valueStress)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.registerReceiver(stressUpdateReceiver, IntentFilter(StressService.ACTION_STRESS_UPDATE))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        context?.unregisterReceiver(stressUpdateReceiver)
    }

    private fun updateStressTextView(stressValue: Int) {
        valueStressTextView.text = stressValue.toString()
    }
}
