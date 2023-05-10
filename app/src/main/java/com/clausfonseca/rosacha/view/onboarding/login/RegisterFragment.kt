package com.clausfonseca.rosacha.view.onboarding.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.databinding.FragmentRegisterBinding
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.extencionFunctions.checkEmptyField
import com.clausfonseca.rosacha.utils.extencionFunctions.cleanErrorValidation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val dialogProgress = DialogProgress()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        initListeners()
        configureComponents()
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
        val passwor2dUser = binding.edtPassword2.text.toString().trim()

        if (email && password && password2) {
            if (passwordUser == passwor2dUser) {
                dialogProgress.show(childFragmentManager, "0")
                registerUser(emailUser, passwordUser)
            } else {
                Util.exibirToast(requireContext(), getString(R.string.check_password))
            }
        }
    }

    private fun registerUser(email: String, password: String) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_global_homeFragment)
                } else {
                    dialogProgress.dismiss()
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