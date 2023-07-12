package com.masbin.myhealth.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.masbin.myhealth.MainAdapterActivity
import com.masbin.myhealth.databinding.ActivitySplashScreenBinding
import com.masbin.myhealth.ui.welcome.WelcomeActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private val delayNumber = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check login status
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            startMainAdapterActivity()
            return // Exit onCreate
        }

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed(
            {
                run {
                    startActivity(Intent(this@SplashScreenActivity, WelcomeActivity::class.java))
                    finish()
                }
            }, delayNumber.toLong()
        )
    }

    private fun startMainAdapterActivity() {
        val intent = Intent(this@SplashScreenActivity, MainAdapterActivity::class.java)
        startActivity(intent)
        finish() // Finish SplashScreenActivity to prevent going back to it
    }
}
