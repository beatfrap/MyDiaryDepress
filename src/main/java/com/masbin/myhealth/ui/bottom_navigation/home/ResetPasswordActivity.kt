package com.masbin.myhealth.ui.bottom_navigation.home

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.masbin.myhealth.R
import com.masbin.myhealth.ui.signin.LoginActivity
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var etNewPassword: EditText
    private lateinit var btnUpdatePassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        etNewPassword = findViewById(R.id.etNewPassword)
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword)

        val email = intent.getStringExtra("email")

        btnUpdatePassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()

            if (newPassword.isEmpty()) {
                Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show()
            } else {
                if (email != null) {
                    updatePasswordOnServer(email, newPassword)
                }
            }
        }
    }

    private fun updatePasswordOnServer(email: String, newPassword: String) {
        val urlString =
            "https://diary-depression.as.r.appspot.com/post/update_password" // Ganti dengan URL server Flask Anda
        val data = JSONObject()
        data.put("email", email)
        data.put("password", newPassword)

        val requestData = data.toString()

        SendDataToServer().execute(urlString, requestData)
    }

    private inner class SendDataToServer : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String? {
            val urlString = params[0]
            val requestData = params[1]

            return try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true

                val outputStream: OutputStream = connection.outputStream
                outputStream.write(requestData.toByteArray())
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    response
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(result: String?) {
            if (result != null) {
                Toast.makeText(
                    this@ResetPasswordActivity,
                    "Password updated successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // Kembali ke halaman login atau halaman lainnya
                val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this@ResetPasswordActivity,
                    "Failed to update password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

