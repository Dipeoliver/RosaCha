package com.example.rosachaclausfonseca.presentation.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.rosachaclausfonseca.R
import com.example.rosachaclausfonseca.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        configureComponents()
    }

    private fun configureComponents() {

        binding.textView.setOnClickListener {


            firebaseAuth.sendPasswordResetEmail("dipeoliver@gmail.com")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "E-mail enviado com sucesso !!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
        }

        binding.button.setOnClickListener {
            val email = binding.emailLayout.text.toString()
            val pass = binding.passwordLayout.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        findNavController().navigate(R.id.action_loginFragment_to_nav_home)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            it.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Empty Fields Are not Allowed !!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
