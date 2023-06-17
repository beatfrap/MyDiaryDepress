package com.masbin.myhealth.ui.bottom_navigation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.masbin.myhealth.databinding.FragmentProfileBinding

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

//    val textView: TextView = binding.textNotifications
//    profileViewModel.text.observe(viewLifecycleOwner, Observer {
//      textView.text = it
//    })
    return root
  }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}