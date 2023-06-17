package com.masbin.myhealth.ui.welcome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.masbin.myhealth.databinding.ActivityWelcomeSecondBinding
import com.masbin.myhealth.ui.signin.AccountActivity

class WelcomeSecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeSecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeSecondBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnNext.setOnClickListener { next()}

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


    private fun next() {
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}