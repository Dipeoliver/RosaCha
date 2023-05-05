package com.clausfonseca.rosacha.view.onboarding.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
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
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.utils.DialogProgress
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
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
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
                dialogProgress.show(childFragmentManager, "0")
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

//    // region - FieldValidation
//    private fun valiEmail(): Boolean {
//        val emailText = binding.edtEmail.text.toString().trim().lowercase()
//        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
//            binding.emailContainer.error = getString(R.string.invalid_email_address)
//            return false
//        }
//        return true
//    }
//
//    private fun textEmailChange() {
//        binding.edtEmail.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {}
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                binding.emailContainer.error = ""
//            }
//        })
//    }
//
//    private fun validPassword(): Boolean {
//        val phoneText = binding.edtPassword.text.toString()
//        if (phoneText == "") {
//            binding.passwordContainer.error = getString(R.string.required_field)
//            return false
//        }
//        if (phoneText.length < 6) {
//            binding.passwordContainer.error = getString(R.string.must_be_6_digits)
//            return false
//        }
//        return true
//    }
//
//    private fun textPasswordChange() {
//        binding.edtPassword.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {}
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                binding.passwordContainer.error = ""
//            }
//        })
//    }
//
//    private fun submitForm() {
//        val email = valiEmail()
//        textEmailChange()
//        val password = validPassword()
//        textPasswordChange()
//
//        val emailuser = binding.edtEmail.text.toString().trim()
//        val passworduser = binding.edtPassword.text.toString().trim()
//
//        if (password && email) {
//            binding.progressBar2.isVisible = true
//            loginUser(emailuser, passworduser)
//        }
//    }
//    // endregion
}