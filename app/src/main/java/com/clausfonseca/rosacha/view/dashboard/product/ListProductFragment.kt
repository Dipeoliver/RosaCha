package com.clausfonseca.rosacha.view.dashboard.product

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentListProductBinding
import com.clausfonseca.rosacha.view.adapter.ProductAdapter
import com.clausfonseca.rosacha.view.helper.FirebaseHelper
import com.clausfonseca.rosacha.view.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class ListProductFragment : Fragment() {

    private var _binding: FragmentListProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private val productlist = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTasks()
    }

    private fun getTasks() {
        FirebaseHelper
            .getDatabase()
            .child("Product")
//            .child(FirebaseHelper.getIdUser() ?: "")
            .child("Product_Item")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        productlist.clear()
                        for (snap in snapshot.children) {
                            val product = snap.getValue(Product::class.java) as Product
                            productlist.add(product)
                        }
                        binding.textinfo.text = ""
//                        productlist.reverse() // trazer a lista do mais novo para o antigo
                        initAdapter()
                    } else {
                        binding.textinfo.text = "Nehuma Tarefa encontrada"

                    }
                    binding.progressBar4.isVisible = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun initAdapter() {
        binding.rvProduct.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProduct.setHasFixedSize(true)
        productAdapter = ProductAdapter(requireContext(), productlist) { task, int ->

        }
        binding.rvProduct.adapter = productAdapter
    }

}