package com.masbin.myhealth.ui.bottom_navigation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.masbin.myhealth.databinding.FragmentProfileBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Logout button click listener
        binding.btnLogout.setOnClickListener {
            logout()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logout() {
        val url = "https://beflask.as.r.appspot.com/post/logout" // Ganti dengan URL endpoint logout pada Flask

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(MediaType.parse("application/json"), "{}"))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                activity?.runOnUiThread {
                    showToast("Logout failed")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body()?.string()
                val jsonObject = JSONObject(responseData)

                val message = jsonObject.getString("message")

                activity?.runOnUiThread {
                    showToast(message)

                    // Perform necessary actions after logout, such as navigating to login screen
                }
            }
        })
    }

    private fun showToast(message: String) {
        // Display a toast message to the user
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
