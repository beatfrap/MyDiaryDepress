package com.masbin.myhealth.ui.bottom_navigation.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.masbin.myhealth.databinding.FragmentProfileBinding
import com.masbin.myhealth.ui.signin.AccountActivity

class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

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

        // Check login status
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (!isLoggedIn) {
            val intent = Intent(requireContext(), AccountActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // Optional: Finish the current activity to prevent going back
        } else {
            // Get user data from SharedPreferences
            val userName = sharedPreferences.getString("username", "")
            val userEmail = sharedPreferences.getString("email", "")
            val userContact = sharedPreferences.getString("userContact", "")
            val userGender = sharedPreferences.getString("gender", "")
            val userBirthdate = sharedPreferences.getString("birthdate", "")

            // Set the data to the respective TextViews
            binding.tvName.text = userName
            binding.tvEmail.text = userEmail
            binding.tvContact.text = userContact
            binding.tvGender.text = userGender
            binding.tvBirth.text = userBirthdate
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logout() {
        // Clear session or perform any logout operations
        // For example, you can clear the login status in SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()

        // Display a toast message to the user
        showToast("Logged out successfully")

        // Start AccountActivity after logout
        val intent = Intent(requireContext(), AccountActivity::class.java)
        startActivity(intent)
        requireActivity().finish() // Optional: Finish the current activity to prevent going back
    }

    private fun showToast(message: String) {
        // Display a toast message to the user
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
