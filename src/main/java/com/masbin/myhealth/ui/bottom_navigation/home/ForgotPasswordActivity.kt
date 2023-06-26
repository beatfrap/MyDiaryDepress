package com.masbin.myhealth.ui.bottom_navigation.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.masbin.myhealth.R

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var btnResetPassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etEmail = findViewById(R.id.etEmail)
        btnResetPassword = findViewById(R.id.btnResetPassword)

        btnResetPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                // Periksa apakah email ada dalam sistem
                if (isEmailExistsInSystem(email)) {
                    // Email ditemukan, tampilkan tampilan mengganti password
                    val intent = Intent(this, ResetPasswordActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                } else {
                    // Email tidak ditemukan
                    Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isEmailExistsInSystem(email: String): Boolean {
        // Lakukan pengecekan email dalam sistem, misalnya dengan query ke database
        // Mengembalikan true jika email ditemukan, false jika tidak
        // Implementasikan logika pengecekan email di sini
        return false
    }
}
