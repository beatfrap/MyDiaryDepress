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

class HeartFragment : Fragment() {
    private lateinit var heartValueTextView: TextView
    private lateinit var heartRateReceiver: BroadcastReceiver

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

        // Inisialisasi dan daftarkan receiver untuk menerima data detak jantung dari HeartService
        heartRateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Dapatkan data detak jantung dari intent dan tampilkan di TextView
                val heartRateValue = intent?.getIntExtra("heartRate", 0) ?: 0
                heartValueTextView.text = heartRateValue.toString()
            }
        }
        context?.registerReceiver(heartRateReceiver, IntentFilter(HeartService.ACTION_HEART_RATE_UPDATE))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hapus pendaftaran receiver saat fragment dihancurkan
        context?.unregisterReceiver(heartRateReceiver)
    }
}
