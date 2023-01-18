package com.clausfonseca.rosacha.view.dashboard.product

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.br.jafapps.bdfirestore.util.DialogProgress
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.databinding.FragmentProductListBinding
import com.clausfonseca.rosacha.model.Product
import com.clausfonseca.rosacha.view.adapter.ProductAdapter
import com.google.firebase.firestore.FirebaseFirestore


class ListProductFragment : Fragment() {

    private lateinit var binding: FragmentProductListBinding
    private lateinit var productAdapter: ProductAdapter
    private val productlist = mutableListOf<Product>()

    var db: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        getProducts()
        initClick()
    }

    override fun onResume() {
        super.onResume()
        getProducts()
    }

    private fun initClick() {
        binding.fabAddProduct.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/addProduct_fragment")
            findNavController().navigate(uri)
        }
    }

    // Firestore DataBase
    private fun getProducts() {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        db!!.collection("Products").get().addOnSuccessListener { results ->

            if (results != null) {
                productlist.clear()

                // result é uma lista
                for (result in results) {
                    val key = result.id // pegar o nome  da pasta do documento
                    val product = result.toObject(Product::class.java)

                    productlist.add(product)
                }
                initAdapter()
                dialogProgress.dismiss()

            } else {
                dialogProgress.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Erro ao exibir o documento, ele não existe",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener { error ->
            dialogProgress.dismiss()
            Toast.makeText(
                requireContext(),
                "Error ${error.message.toString()}",
                Toast.LENGTH_SHORT
            ).show()
        }

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