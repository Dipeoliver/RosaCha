package com.clausfonseca.rosacha.view.dashboard

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        binding.homeCons.visibility = View.GONE
        initClicks()
    }


    private fun initClicks() {
        binding.ibLogout.setOnClickListener {
            logoutApp()
        }
    }

    private fun logoutApp() {
        auth.signOut()
        findNavController().popBackStack()
        val uri = Uri.parse("android-app://com.clausfonseca.rosacha/login_fragment")
        findNavController().navigate(uri)
    }
}