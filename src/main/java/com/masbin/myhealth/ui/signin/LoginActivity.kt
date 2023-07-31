package com.masbin.myhealth.ui.signin

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.masbin.myhealth.MainAdapterActivity
import com.masbin.myhealth.R
import com.masbin.myhealth.databinding.ActivityLoginBinding
import com.masbin.myhealth.service.HeartService
import com.masbin.myhealth.ui.bottom_navigation.home.ForgotPasswordActivity
import org.json.JSONException
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.graphics.drawable.Drawable
import android.widget.CompoundButton
import kotlinx.android.synthetic.main.activity_login.*
import kotlin.math.log

object UserManager {
    private var userId: Int = -1 // Default value, bisa diganti sesuai kebutuhan

    fun setUserId(id: Int) {
        userId = id
    }

    fun getUserId(): Int {
        return userId
    }

    fun isLoggedIn(): Boolean {
        return userId != -1
    }
}


class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnLupaPassword: Button
    private var isPasswordVisible = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLupaPassword = findViewById(R.id.btnLupaPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            } else {
                val data = JSONObject()
                data.put("username", username)
                data.put("password", password)

                SendDataLoginToServer().execute(data.toString())
            }
        }
        btnLupaPassword.setOnClickListener { lupaPassword() }

        // Check login status when the app starts
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            val intent = Intent(this@LoginActivity, MainAdapterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish() // Don't go back to LoginActivity if already logged in
        }

        togglePassword.setOnCheckedChangeListener { _, isChecked ->
            isPasswordVisible = isChecked
            updatePasswordVisibility()
        }
    }

    private fun updatePasswordVisibility() {
        etPassword.transformationMethod = if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()
    }

    private fun lupaPassword() {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SendDataLoginToServer : AsyncTask<String, Void, String>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: String): String? {
            val urlString = "https://beflask.as.r.appspot.com/post/login" // Replace with your Flask server URL
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

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: String?) {
            if (result != null) {
                try {
                    val response = JSONObject(result)
                    val message = response.getString("message")
                    if (message == "Login successful") {
                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()

                        // Dapatkan ID pengguna dari respons JSON
                        val userId = response.getInt("id")
                        UserManager.setUserId(userId)
                        // Simpan status login dan ID pengguna ke Shared Preferences
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("isLoggedIn", true)
                        editor.putInt("id", userId) // Simpan ID ke Shared Preferences
                        editor.apply()
                        val intent = Intent(this@LoginActivity, MainAdapterActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish() // Jangan kembali ke LoginActivity setelah login berhasil
                    } else {
                        Toast.makeText(this@LoginActivity, "Username atau password salah", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this@LoginActivity, "Respon server tidak valid", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@LoginActivity, "Gagal login", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun logout() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()
    }

    // Example usage: when the logout button is clicked
    private fun handleLogout() {
        logout()
    }
}
