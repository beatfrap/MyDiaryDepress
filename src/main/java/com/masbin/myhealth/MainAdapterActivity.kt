package com.masbin.myhealth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.masbin.myhealth.databinding.ActivityMainAdapterBinding

class MainAdapterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainAdapterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainAdapterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val navController = findNavController(R.id.nav_host_fragment_activity_main2)

        binding.navView.setupWithNavController(navController)
    }

}