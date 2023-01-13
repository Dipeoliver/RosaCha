package com.clausfonseca.rosacha.view.onboarding

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentSplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashFragment : Fragment() {

    private lateinit var binding: FragmentSplashBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Tempo da splah
        Handler(Looper.getMainLooper()).postDelayed(this::checkAuth, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    // verificar se usuario esta logado
    private fun checkAuth() {
        auth = Firebase.auth
        if (auth.currentUser == null) {
            findNavController().navigate(R.id.action_splashFragment_to_authentication)
        } else {
            findNavController().navigate(R.id.action_splashFragment_to_homeFragment2)

        }
    }
}