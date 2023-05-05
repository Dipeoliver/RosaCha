package com.clausfonseca.rosacha.view.onboarding.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.databinding.FragmentLoginBinding
import com.clausfonseca.rosacha.utils.extencionFunctions.checkEmptyField
import com.clausfonseca.rosacha.utils.extencionFunctions.cleanErrorValidation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        initListeners()
    }

    private fun initListeners() {
        binding.btnLogin.setOnClickListener {
            submitForm()
        }
        binding.btnRegisterAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        binding.btnRecover.setOnClickListener {
            val args = Bundle()
            args.putString("email", binding.edtEmail.text.toString().trim())
            findNavController().navigate(R.id.action_loginFragment_to_recoverFragment, args)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_global_homeFragment)
                } else {
                    binding.progressBar2.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        FirebaseHelper.validError(task.exception?.message ?: ""),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun submitForm() {
        val email = checkEmptyField(binding.edtEmail, binding.emailContainer, requireContext(), "email")
        cleanErrorValidation(binding.edtEmail, binding.emailContainer)
        val password = checkEmptyField(binding.edtPassword, binding.passwordContainer, requireContext(), "password")
        cleanErrorValidation(binding.edtPassword, binding.passwordContainer)

        val emailUser = binding.edtEmail.text.toString().trim()
        val passwordUser = binding.edtPassword.text.toString().trim()

        if (password && email) {
            binding.progressBar2.isVisible = true
            loginUser(emailUser, passwordUser)
        }
    }

}