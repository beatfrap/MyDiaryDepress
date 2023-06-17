package com.masbin.myhealth.ui.welcome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.masbin.myhealth.databinding.ActivityWelcomeFirstBinding
import com.masbin.myhealth.ui.signin.AccountActivity

class WelcomeFirstActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeFirstBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnNext.setOnClickListener { next() }
        binding.btnSkip.setOnClickListener { skip() }

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun next() {
        val intent = Intent(this, WelcomeSecondActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun skip() {
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}