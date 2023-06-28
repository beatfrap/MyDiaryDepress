package com.masbin.myhealth.ui.bottom_navigation.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.masbin.myhealth.R

class ConnectBleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_ble)
        supportActionBar?.hide()

    }
}