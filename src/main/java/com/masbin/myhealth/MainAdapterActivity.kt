package com.masbin.myhealth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.masbin.myhealth.databinding.ActivityMainAdapterBinding
import android.os.PowerManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import com.masbin.myhealth.service.SmartbandService

class MainAdapterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainAdapterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainAdapterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val navController = findNavController(R.id.nav_host_fragment_activity_main2)

        binding.navView.setupWithNavController(navController)

        // Mulai HeartService dan StressService saat Activity dibuat
        startService()
    }

    private fun startService() {
        val intent = Intent(this, SmartbandService::class.java)
        startService(intent)
    }
}
