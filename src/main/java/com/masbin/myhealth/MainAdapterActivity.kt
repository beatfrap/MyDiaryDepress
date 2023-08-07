package com.masbin.myhealth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.masbin.myhealth.databinding.ActivityMainAdapterBinding
import com.masbin.myhealth.service.HeartService
import com.masbin.myhealth.service.SleepService
import com.masbin.myhealth.service.StressService
import android.os.PowerManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

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
        startHeartService()
        startStressService()
        startSleepService()
    }

    private fun startSleepService() {
        val intent = Intent(this, SleepService::class.java)
        startService(intent)
    }

    private fun startHeartService() {
        val intent = Intent(this, HeartService::class.java)
        startService(intent)
    }

    private fun startStressService() {
        val intent = Intent(this, StressService::class.java)
        startService(intent)
    }
}
