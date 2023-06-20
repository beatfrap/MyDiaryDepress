package com.masbin.myhealth.ui.signin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.masbin.myhealth.R
import android.os.AsyncTask
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.masbin.myhealth.MainAdapterActivity
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class RegisterActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etContact: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etContact = findViewById(R.id.etContact)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val contact = etContact.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            } else {
                val data = JSONObject()
                data.put("username", username)
                data.put("email", email)
                data.put("password", password)
                data.put("emergency_contact", contact)

                SendDataToServer().execute(data.toString())
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    private inner class SendDataToServer : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String? {
            val urlString = "https://beflask.as.r.appspot.com/post/register" // Ganti dengan URL server Flask Anda
            val jsonData = params[0]

            return try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true

                val outputStream: OutputStream = connection.outputStream
                outputStream.write(jsonData.toByteArray())
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
                Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this@RegisterActivity, "Failed to register", Toast.LENGTH_SHORT).show()
            }
        }

    }
}