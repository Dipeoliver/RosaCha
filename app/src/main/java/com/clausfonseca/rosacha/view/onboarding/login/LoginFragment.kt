package com.clausfonseca.rosacha.view.onboarding.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.databinding.FragmentLoginBinding
import com.clausfonseca.rosacha.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth

    private val viewModel: LoginViewModel by viewModels()

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
        configureObservables()
    }

    private fun initListeners() {
        binding.btnLogin.setOnClickListener {
//            submitForm()
            val emailUser = binding.edtEmail.text.toString().trim()
            val passwordUser = binding.edtPassword.text.toString().trim()
            viewModel.signIn(emailUser, passwordUser)
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

    private fun configureObservables() {
        viewModel.model.screenState.observe(viewLifecycleOwner, Observer {
            handleState(it)
        })
    }

    private fun handleState(state: LoginModelState.LoginState?) {
        when (state) {
            is LoginModelState.LoginState.Loading -> binding.progressBar2.isVisible = true

            is LoginModelState.LoginState.Success -> {
                binding.progressBar2.isVisible = false
                findNavController().navigate(R.id.action_global_homeFragment)
            }

            is LoginModelState.LoginState.Error -> {

                binding.progressBar2.isVisible = false
                Toast.makeText(
                        requireContext(),
                        FirebaseHelper.validError(state.message),
                        Toast.LENGTH_SHORT
                    ).show()
            }

            else -> {}
        }
    }
}