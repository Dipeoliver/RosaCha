package com.example.rosachaclausfonseca.presentation.dashboard.price

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.rosachaclausfonseca.databinding.FragmentPriceBinding

class PriceFragment : Fragment() {

    private var _binding: FragmentPriceBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val priceViewModel =
            ViewModelProvider(this).get(PriceViewModel::class.java)

        _binding = FragmentPriceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}