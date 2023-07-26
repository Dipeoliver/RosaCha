package com.clausfonseca.rosacha.view.onboarding.recover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.databinding.FragmentRecoverBinding
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.extencionFunctions.checkEmptyField
import com.clausfonseca.rosacha.utils.extencionFunctions.cleanErrorValidation
import com.clausfonseca.rosacha.view.onboarding.login.LoginModelState
import com.clausfonseca.rosacha.view.onboarding.login.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecoverFragment : Fragment() {
    private lateinit var binding: FragmentRecoverBinding
    private lateinit var receivedArgs: String
    val dialogProgress = DialogProgress()

    private val viewModel: RecoverViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        receivedArgs = requireArguments().get("email").toString()
        binding.edtEmail.setText(receivedArgs)
        configureComponents()
        configureObservables()
    }

    private fun configureComponents() {
        binding.apply {
            edtEmail.requestFocus()
            (activity as AppCompatActivity?)?.setSupportActionBar(toolbarRecovery)
            toolbarRecovery.setNavigationIcon(com.clausfonseca.rosacha.R.drawable.ic_back)
//            (activity as AppCompatActivity?)?.supportActionBar?.setTitle(getString(com.clausfonseca.rosacha.R.string.recovery_account));
            toolbarRecovery.setNavigationOnClickListener {
                findNavController().navigate(com.clausfonseca.rosacha.R.id.action_recoverFragment_to_loginFragment)
            }
        }
    }

    private fun initListeners() {
        binding.btnEnviar.setOnClickListener {
            submitForm()
        }
    }

    private fun submitForm() {
        val email = checkEmptyField(binding.edtEmail, binding.emailContainer, requireContext(), "email")
        cleanErrorValidation(binding.edtEmail, binding.emailContainer)
        val emailUser = binding.edtEmail.text.toString().trim()
        if (email) {
            viewModel.recoverPassword(emailUser)
        }
    }

    private fun configureObservables() {
        viewModel.model.screenState.observe(viewLifecycleOwner, Observer {
            handleState(it)
        })
    }

    private fun handleState(state: RecoverModelState.RecoverState?) {
        when (state) {
            is RecoverModelState.RecoverState.Loading -> {
//                if (!dialogProgress.isAdded) dialogProgress.show(childFragmentManager, "1")
            }

            is RecoverModelState.RecoverState.Success -> {
                Toast.makeText(
                    requireContext(),
                    getString(com.clausfonseca.rosacha.R.string.sent_email),
                    Toast.LENGTH_SHORT
                ).show()
//                dialogProgress.dismiss()
//                findNavController().navigate(R.id.action_global_homeFragment)
                findNavController().popBackStack()
            }

            is RecoverModelState.RecoverState.Error -> {
//                dialogProgress.dismiss()
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