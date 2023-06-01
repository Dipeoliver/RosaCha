package com.clausfonseca.rosacha.view.onboarding.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.databinding.FragmentRecoverBinding
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.extencionFunctions.checkEmptyField
import com.clausfonseca.rosacha.utils.extencionFunctions.cleanErrorValidation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RecoverFragment : Fragment() {
    private lateinit var binding: FragmentRecoverBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var receivedArgs: String
    val dialogProgress = DialogProgress()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRecoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        initListeners()
        receivedArgs = requireArguments().get("email").toString()
        binding.edtEmail.setText(receivedArgs)
        configureComponents()
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
            dialogProgress.show(childFragmentManager, "0")
            submitForm()
        }
    }

    private fun submitForm() {
        val email = checkEmptyField(binding.edtEmail, binding.emailContainer, requireContext(), "email")
        cleanErrorValidation(binding.edtEmail, binding.emailContainer)

        val emailUser = binding.edtEmail.text.toString().trim()

        if (email) {
            recoveryUser(emailUser)
        }
    }

    private fun recoveryUser(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        getString(com.clausfonseca.rosacha.R.string.sent_email),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        FirebaseHelper.validError(task.exception?.message ?: ""),
                        Toast.LENGTH_LONG
                    ).show()
                }
                dialogProgress.dismiss()
            }
    }
}