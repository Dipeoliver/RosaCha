package com.clausfonseca.rosacha.view.onboarding.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentRegisterBinding
import com.clausfonseca.rosacha.view.helper.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        initClicks()
        configureComponents()
        binding.edtEmail.requestFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun configureComponents() {
        binding.apply {
            edtEmail.requestFocus()
            (activity as AppCompatActivity?)?.setSupportActionBar(toolbarRegister)
            toolbarRegister.setNavigationIcon(com.clausfonseca.rosacha.R.drawable.ic_back)
//            (activity as AppCompatActivity?)?.supportActionBar?.setTitle(getString(com.clausfonseca.rosacha.R.string.recovery_account));
            toolbarRegister.setNavigationOnClickListener {
                findNavController().navigate(com.clausfonseca.rosacha.R.id.action_registerFragment_to_loginFragment)
            }
        }
    }

    private fun initClicks() {
        binding.btnRegister.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()
        val password2 = binding.edtPassword2.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty() && password2.isNotEmpty()) {
            if (password == password2) {
                binding.progressBar.isVisible = true
                registerUser(email, password)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.check_password),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.empty_fields), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun registerUser(email: String, password: String) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_global_homeFragment)
                } else {
                    binding.progressBar.isVisible = false
//                    Log.i("INFOTEST", "loginUser: ${task.exception?.message}")
                    Toast.makeText(
                        requireContext(),
                        FirebaseHelper.validError(task.exception?.message ?: ""),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}