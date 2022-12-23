package com.clausfonseca.rosacha.view.dashboard.product

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.clausfonseca.rosacha.databinding.FragmentListProductBinding
import com.clausfonseca.rosacha.view.adapter.ProductAdapter
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.model.Product
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
        productAdapter = ProductAdapter(requireContext(), productlist) { product, select ->
            optionSelect(product, select)
        }
        binding.rvProduct.adapter = productAdapter
    }

    private fun optionSelect(product: Product, select: Int) {
        when (select) {
            ProductAdapter.SELECT_REMOVE -> {
                deleteProduct(product)
            }
//            ClientAdapter.SELECT_EDIT -> {
//                val action = HomeFragmentDirections
//                    .actionHomeFragmentToFormTaskFragment(task)
//                findNavController().navigate(action)
//            }
        }

    }

    private fun deleteProduct(client: Product) {
        FirebaseHelper
            .getDatabase()
            .child("Product")
            .child("Product_Item")
            .child(client.id)
            .removeValue()
    }
}