package com.clausfonseca.rosacha.view.dashboard.product

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.br.jafapps.bdfirestore.util.DialogProgress
import com.br.jafapps.bdfirestore.util.Util
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.databinding.FragmentProductListBinding
import com.clausfonseca.rosacha.model.Product
import com.clausfonseca.rosacha.view.adapter.ProductAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ListProductFragment : Fragment(), ProductAdapter.LastItemRecyclerView {

    private lateinit var binding: FragmentProductListBinding
    private lateinit var productAdapter: ProductAdapter
    private val productlist = mutableListOf<Product>()

    var db: FirebaseFirestore? = null
    var nextquery: Query? = null

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
        initClick()
        initAdapter()
        getProducts()
    }

    override fun onResume() {
        super.onResume()
        getProducts()
    }

    override fun lastItemRecyclerView(isShow: Boolean) {
        getMoreProducts()
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

        db!!.collection("Products").orderBy("description").limit(10).get().addOnSuccessListener { results ->
            dialogProgress.dismiss()

            if (results != null) {
                productlist.clear()

                // result é uma lista
                for (result in results) {
                    val key = result.id // pegar o nome  da pasta do documento
                    val product = result.toObject(Product::class.java)
                    productlist.add(product)
                }
                // pegar ultimo item da query
                val lastresult = results.documents[results.size() - 1]
                nextquery = db!!.collection("Products").orderBy("description").startAfter(lastresult).limit(10)

                productAdapter.notifyDataSetChanged()
//                initAdapter()
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

    private fun getMoreProducts() {
        nextquery?.get()?.addOnSuccessListener { results ->

            // o if e para verificar se chegou o fim da lista
            if (results.size() > 0) {
                // pegar ultimo item da query
                val lastresult = results.documents[results.size() - 1]

                nextquery = db!!.collection("Products").orderBy("description").startAfter(lastresult).limit(10)

                for (result in results) {
                    val product = result.toObject(Product::class.java)
                    productlist.add(product)
                }
                // notificar que teve atualizalçao
                productAdapter.notifyDataSetChanged()
            } else {
//                Util.exibirToast(requireContext(), "Não ha mais itens para serem exibidos")
            }
        }?.addOnFailureListener() { error ->
            Util.exibirToast(requireContext(), error.message.toString())
        }
    }

    private fun initAdapter() {
        binding.rvProduct.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProduct.setHasFixedSize(true)
        productAdapter = ProductAdapter(requireContext(), productlist,this) { product, select ->
            optionSelect(product, select)
        }
        binding.rvProduct.adapter = productAdapter
    }

    private fun optionSelect(product: Product, select: Int) {
        when (select) {
            ProductAdapter.SELECT_REMOVE -> {
                configDialog(product)
            }
            ProductAdapter.SELECT_EDIT -> {
            }
        }
    }

    private fun configDialog(product: Product) {

        val builder = AlertDialog.Builder(requireContext())

        //set title for alert dialog
//        builder.setTitle("Atenção")
        builder.setTitle(Html.fromHtml("<font color='#FB2391'>Atenção</font>"));

        //set message for alert dialog
        builder.setMessage("Realmente deseja excluir: ${product.description}")
        builder.setIcon(R.drawable.ic_warning)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            deleteProduct(product)
        }
//        //performing cancel action
//        builder.setNeutralButton("Cancel"){dialogInterface , which ->
//            Toast.makeText(applicationContext,"clicked cancel\n operation cancel",Toast.LENGTH_LONG).show()
//        }
        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun deleteProduct(product: Product) {
        FirebaseHelper
            .getDatabase()
            .child("Product")
            .child("Product_Item")
            .child(product.id)
            .removeValue()
    }


}