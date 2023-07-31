package com.masbin.myhealth.ui.bottom_navigation.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.masbin.myhealth.R
import com.masbin.myhealth.databinding.FragmentHealthBinding

class HealthFragment : Fragment() {

    private lateinit var healthViewModel: HealthViewModel
    private var _binding: FragmentHealthBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        healthViewModel = ViewModelProvider(this).get(HealthViewModel::class.java)

        _binding = FragmentHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvStress.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_stressFragment)
        }
        binding.tvDepress.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_depressFragment)
        }
        binding.tvHeartRate.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_heartFragment)
        }
        binding.tvSleep.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_sleepFragment)
        }
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
}