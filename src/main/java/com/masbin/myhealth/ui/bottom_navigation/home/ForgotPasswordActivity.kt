package com.masbin.myhealth.ui.bottom_navigation.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.masbin.myhealth.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var btnResetPassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        supportActionBar?.hide()
        etEmail = findViewById(R.id.etEmail)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        btnResetPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Tolong Isi Email Kamu", Toast.LENGTH_SHORT).show()
            } else {
                val data = JSONObject()
                data.put("email", email)

                // Kirim permintaan ke server untuk memeriksa validitas email
                val url = "https://diary-depression.as.r.appspot.com/post/forget/password"
                val client = OkHttpClient()
                val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), data.toString())
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        // Gagal menghubungi server
                        runOnUiThread {
                            Toast.makeText(this@ForgotPasswordActivity, "Failed to connect to server", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        response.close()

                        if (response.isSuccessful) {
                            try {
                                val jsonResponse = JSONObject(responseBody)
                                val message = jsonResponse.getString("message")
                                if (message == "Email is valid") {
                                    // Email valid, lanjut ke tampilan penggantian password

                                    val intent = Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
                                    intent.putExtra("email", email)
                                    startActivity(intent)
                                    runOnUiThread {
                                        Toast.makeText(this@ForgotPasswordActivity, "Email Valid", Toast.LENGTH_SHORT).show()
                                    }

                                } else {
                                    // Email tidak valid
                                    runOnUiThread {
                                        Toast.makeText(this@ForgotPasswordActivity, "Email not found", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            // Respons tidak sukses dari server
                            runOnUiThread {
                                Toast.makeText(this@ForgotPasswordActivity, "Email Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }
        }
    }
}
