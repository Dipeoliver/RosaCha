package com.example.rosachaclausfonseca.ui.inventory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.rosachaclausfonseca.databinding.FragmentInventoryBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class InventoryFragment : Fragment() {

    private lateinit var _binding: FragmentInventoryBinding
    private var scannedResult = ""


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(InventoryViewModel::class.java)

        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textInventory
        slideshowViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        configureButton()
        return root

    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                binding.txtValue.text = result.contents
            } else {
                binding.txtValue.text = "scan failed"
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @Suppress("DEPRECATION")
    private fun configureButton() {
        binding.btnScan.setOnClickListener {
            run {
                IntentIntegrator(this@InventoryFragment.activity).initiateScan()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //_binding = null
    }
}