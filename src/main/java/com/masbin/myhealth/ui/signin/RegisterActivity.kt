package com.masbin.myhealth.ui.signin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.masbin.myhealth.R
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etContact: EditText
    private lateinit var etPassword: EditText
    private lateinit var etNameContact: EditText
    private lateinit var etGender: EditText
    private lateinit var etBirthdate: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etGender = findViewById(R.id.etGender)
        etBirthdate = findViewById(R.id.etBrith) // Fix typo here
        etPassword = findViewById(R.id.etPassword)
        etNameContact = findViewById(R.id.etNameContact)
        etContact = findViewById(R.id.etContact)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val gender = etGender.text.toString()
            val birthdate = etBirthdate.text.toString()
            val nameContact = etNameContact.text.toString()
            val contact = etContact.text.toString()

            // Create a JSON object to hold the user registration data
            val jsonData = JSONObject()
            jsonData.put("username", username)
            jsonData.put("email", email)
            jsonData.put("password", password)
            jsonData.put("gender", gender)
            jsonData.put("birthdate", birthdate)
            jsonData.put("name_contact", nameContact)
            jsonData.put("contact", contact)

            // Execute the AsyncTask to register the user
            RegisterAsyncTask().execute(jsonData.toString())
        }
    }

    // AsyncTask to handle the registration request
    @SuppressLint("StaticFieldLeak")
    private inner class RegisterAsyncTask : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg params: String): Boolean {
            val urlString = "https://diary-depression.as.r.appspot.com/post/register" // Replace with your actual Flask server IP
            val jsonData = params[0]

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val outputStream: OutputStream = connection.outputStream
            val writer = BufferedWriter(OutputStreamWriter(outputStream))
            writer.write(jsonData)
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            return responseCode == HttpURLConnection.HTTP_CREATED
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                // Registration successful, start LoginActivity
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish() // Optional: If you want to finish the RegisterActivity after registration
            } else {
                Toast.makeText(this@RegisterActivity, "Error registering user", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
