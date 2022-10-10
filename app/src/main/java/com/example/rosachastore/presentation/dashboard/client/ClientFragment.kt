package com.example.rosachastore.presentation.dashboard.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.rosachaclausfonseca.presentation.dashboard.client.ClientViewModel
import com.example.rosachastore.databinding.FragmentClientBinding

class ClientFragment : Fragment() {

    private var _binding: FragmentClientBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val clientViewModel =
            ViewModelProvider(this).get(ClientViewModel::class.java)

        _binding = FragmentClientBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textClient
        clientViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}