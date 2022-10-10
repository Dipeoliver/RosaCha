package com.example.rosachastore.presentation.dashboard.product

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.rosachaclausfonseca.presentation.dashboard.product.AddProductViewModel
import com.example.rosachastore.databinding.FragmentAddProductBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class AddProductFragment : Fragment() {

    private lateinit var binding: FragmentAddProductBinding
    private var scannedResult = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(AddProductViewModel::class.java)

        binding = FragmentAddProductBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureButton()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                binding.edtBarcode.text = result.contents
                binding.edtReference.requestFocus()

            } else {
                binding.edtBarcode.text = "scan failed"
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            binding.edtBarcode.requestFocus()
        }
    }

    @Suppress("DEPRECATION")
    private fun configureButton() {
        binding.btnAddProductScan.setOnClickListener {
            val integrator: IntentIntegrator =
                IntentIntegrator.forSupportFragment(this@AddProductFragment)
            integrator.setPrompt("Scanner Ativo")
            integrator.initiateScan()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //_binding = null
    }
}