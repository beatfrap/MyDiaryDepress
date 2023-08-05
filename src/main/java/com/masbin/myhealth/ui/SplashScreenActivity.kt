package com.masbin.myhealth.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.masbin.myhealth.MainAdapterActivity
import com.masbin.myhealth.databinding.ActivitySplashScreenBinding
import com.masbin.myhealth.ui.welcome.WelcomeActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private var binding: ActivitySplashScreenBinding? = null
    private val delayNumber = 2000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check login status
        val isLoggedIn =
            getSharedPreferences("MyPrefs", MODE_PRIVATE).getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            startMainAdapterActivity()
            return  // Exit onCreate
        }

        // Request to ignore battery optimizations
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        supportActionBar!!.hide()
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@SplashScreenActivity, WelcomeActivity::class.java))
            finish()
        }, delayNumber.toLong())
    }

    private fun startMainAdapterActivity() {
        val intent = Intent(this, MainAdapterActivity::class.java)
        startActivity(intent)
        finish() // Finish SplashScreenActivity to prevent going back to it
    }
}