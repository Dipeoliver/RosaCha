package com.clausfonseca.rosacha.ui.dashboard.product

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentAddProductBinding
import com.clausfonseca.rosacha.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        configureButton()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                binding.edtBarcodeProduct.setText(result.contents)
                binding.edtReferenceProduct.requestFocus()

            } else {
                binding.edtBarcodeProduct.setText("scan failed")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            binding.edtBarcodeProduct.requestFocus()
        }
    }

    @Suppress("DEPRECATION")
    private fun configureButton() {
        binding.btnScan.setOnClickListener {
            val integrator: IntentIntegrator =
                IntentIntegrator.forSupportFragment(this@AddProductFragment)
            integrator.setPrompt("Scanner Ativo")
            integrator.initiateScan()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}