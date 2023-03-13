package com.clausfonseca.rosacha.view.dashboard.product

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentProductListBinding
import com.clausfonseca.rosacha.model.Product
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.view.adapter.ProductAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class ListProductFragment : Fragment(), ProductAdapter.LastItemRecyclerView, ProductAdapter.ClickProduto {

    private lateinit var binding: FragmentProductListBinding
    private lateinit var productAdapter: ProductAdapter
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private var dbProducts: String = ""

    private val productlist = mutableListOf<Product>()
    var db: FirebaseFirestore? = null
    var nextquery: Query? = null
    var isFilterOn = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        firebaseStorage = Firebase.storage
        auth = Firebase.auth
        dbProducts = getString(R.string.db_product).toString()
        onBackPressed()
        initListeners()
        initAdapter()
        getProducts()
        searchProduct()
    }

//    override fun onResume() {
//        super.onResume()
//        getProducts()
//    }

    override fun clickProduto(product: Product) {
        selectedProduct(product)
    }

    private fun selectedProduct(product: Product) {
        findNavController().navigate(ProductFragmentDirections.actionProductFragmentToEditProductFragment(product))
    }

    override fun lastItemRecyclerView(isShow: Boolean) {
        if (isFilterOn)
        else getMoreProducts()
    }

    // ao clicar botão voltar abaixo

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/home_fragment")
                findNavController().navigate(uri)
            }
        })
    }


    private fun initListeners() {
        binding.fabAddProduct.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/addProduct_fragment")
            findNavController().navigate(uri)
        }
    }

    // To control the click into searchView
    private fun searchProduct() {
        binding.svProduct.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        binding.svProduct.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("Diego-onQueryTextSubmit", query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                isFilterOn = true
                filterSearchProduct(newText.toString())
                Log.d("Diego-onQueryTextChange", newText.toString())
                return true
            }
        })
        binding.svProduct.setOnCloseListener(object : SearchView.OnCloseListener,
            android.widget.SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                binding.svProduct.onActionViewCollapsed()
                productlist.clear()
                productAdapter.notifyDataSetChanged()
                getProducts()
                isFilterOn = false
                return true
            }
        })
    }

    // Firestore DataBase -----------------------------------------------
    private fun filterSearchProduct(newText: String) {
        db!!.collection(dbProducts).orderBy("description").startAt(newText)
            .endAt(newText + "\uf8ff")?.limit(5)?.get()?.addOnSuccessListener { results ->
                if (results.size() > 0) {
                    productlist.clear()
                    for (result in results) {
                        val product = result.toObject(Product::class.java)
                        productlist.add(product)
                    }
                    productAdapter.notifyDataSetChanged()
                }
            }?.addOnFailureListener { error ->
                Toast.makeText(
                    requireContext(),
                    "Error ${error.message.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // Firestore DataBase
    private fun getProducts() {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        db!!.collection(dbProducts).orderBy("description").limit(10).get().addOnSuccessListener { results ->
            dialogProgress.dismiss()


            if (results.size() > 0) {
                productlist.clear()

                // result é uma lista
                for (result in results) {
                    val product = result.toObject(Product::class.java)
                    productlist.add(product)
                }
                // pegar ultimo item da query
                val lastresult = results.documents[results.size() - 1]
                nextquery = db!!.collection(dbProducts).orderBy("description").startAfter(lastresult).limit(10)

                productAdapter.notifyDataSetChanged()
//                initAdapter()
            } else {
                dialogProgress.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Não existem produtos para serem exibidos",
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

                nextquery = db!!.collection(dbProducts).orderBy("description").startAfter(lastresult).limit(10)

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
    // END Firestore DataBase -------------------------------------------

    private fun initAdapter() {
        binding.rvProduct.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProduct.setHasFixedSize(true)
        productAdapter = ProductAdapter(requireContext(), productlist, this, this) { product, select ->
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

    // Delete Product  ---------------------------------------------------
    private fun configDialog(product: Product) {

        val builder = AlertDialog.Builder(requireContext())

        //set title for alert dialog
//        builder.setTitle("Atenção")
        builder.setTitle(Html.fromHtml("<font color='#FB2391'>Atenção</font>"));

        //set message for alert dialog
        builder.setMessage("Realmente deseja excluir: ${product.description}")
        builder.setIcon(R.drawable.baseline_warning_24)

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
        val reference = db!!.collection(dbProducts)
        product.barcode?.let {
            reference.document(it).delete().addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    removeImage(product.barcode!!)
                    Util.exibirToast(requireContext(), "Deletado com Sucesso")
                    getProducts()
                } else {
                    Util.exibirToast(requireContext(), "erro ao deletar no banco ${task.exception.toString()}")
                }
            }
        }
    }

    fun removeImage(barcode: String) {
        val reference = firebaseStorage.reference.child(dbProducts).child("${barcode}.jpg")
        reference.delete().addOnSuccessListener { task ->
        }.addOnFailureListener { error ->
            Util.exibirToast(requireContext(), "Falha ao deletar a imagem ${error.message.toString()}")
        }
    }
// END Delete Product ----------------------------------------------------

}