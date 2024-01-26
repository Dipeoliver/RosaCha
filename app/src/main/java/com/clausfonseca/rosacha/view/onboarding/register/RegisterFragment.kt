package com.clausfonseca.rosacha.view.onboarding.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.databinding.FragmentRegisterBinding
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.extencionFunctions.checkEmptyField
import com.clausfonseca.rosacha.utils.extencionFunctions.cleanErrorValidation
import com.clausfonseca.rosacha.view.onboarding.CommonModelState
import com.clausfonseca.rosacha.view.onboarding.recover.RecoverModelState
import com.clausfonseca.rosacha.view.onboarding.recover.RecoverViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val dialogProgress = DialogProgress()
    private val viewModel: RegisterViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        configureComponents()
        configureObservables()
    }

    private fun configureComponents() {
        binding.apply {
            (activity as AppCompatActivity?)?.setSupportActionBar(toolbarRegister)
            toolbarRegister.setNavigationIcon(com.clausfonseca.rosacha.R.drawable.ic_back)
//            (activity as AppCompatActivity?)?.supportActionBar?.setTitle(getString(com.clausfonseca.rosacha.R.string.recovery_account));
            toolbarRegister.setNavigationOnClickListener {
                findNavController().navigate(com.clausfonseca.rosacha.R.id.action_registerFragment_to_loginFragment)
            }
        }
    }

    private fun initListeners() {
        binding.btnRegister.setOnClickListener {
            submitForm()
        }
    }

    private fun submitForm() {
        val email = checkEmptyField(binding.edtEmail, binding.emailContainer, requireContext(), "email")
        cleanErrorValidation(binding.edtEmail, binding.emailContainer)

        val password = checkEmptyField(binding.edtPassword, binding.passwordContainer, requireContext(), "password")
        cleanErrorValidation(binding.edtPassword, binding.passwordContainer)

        val password2 = checkEmptyField(binding.edtPassword2, binding.passwordContainer2, requireContext(), "password")
        cleanErrorValidation(binding.edtPassword2, binding.passwordContainer2)


        val emailUser = binding.edtEmail.text.toString().trim()
        val passwordUser = binding.edtPassword.text.toString().trim()
        val password2dUser = binding.edtPassword2.text.toString().trim()

        if (email && password && password2) {
            if (passwordUser == password2dUser) {//
                viewModel.registerUser(emailUser, passwordUser)//
            } else {
                Util.exibirToast(requireContext(), getString(R.string.check_password))
            }
        }
    }

    private fun configureObservables() {
        viewModel.model.screenState.observe(viewLifecycleOwner, Observer {
            handleState(it)
        })
    }
    private fun progressState(isloading: Boolean){
        if (isloading) dialogProgress.show(childFragmentManager, "0")
        else dialogProgress.dismiss()
    }
    private fun handleState(state: CommonModelState.CommonState?) {
        when (state) {
            is CommonModelState.CommonState.Loading -> {
                progressState(state.isLoading)
            }

            is CommonModelState.CommonState.Success -> {
                findNavController().navigate(R.id.action_global_homeFragment)
            }

            is CommonModelState.CommonState.Error -> {
                Toast.makeText(
                    requireContext(),
                    FirebaseHelper.validError(state.message),
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                println()
            }
        }
    }
}