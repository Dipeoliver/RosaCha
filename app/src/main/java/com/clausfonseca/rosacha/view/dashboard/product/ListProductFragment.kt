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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentProductListBinding
import com.clausfonseca.rosacha.model.Product
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Swipe.SwipeGesture
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.view.adapter.ProductAdapter
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class ListProductFragment : Fragment(), ProductAdapter.LastItemRecyclerView {

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
        dbProducts = getString(R.string.db_product)
        initListeners()
        initAdapter()
        getProducts()
        searchProduct()
        onBackPressed()
    }

    override fun lastItemRecyclerView(isShow: Boolean) {
        if (isFilterOn)
        else getMoreProducts()
    }

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

    private fun selectedProduct(product: Product) {
        findNavController().navigate(ProductFragmentDirections.actionProductFragmentToEditProductFragment(product))
    }

    private fun initAdapter() {
        binding.rvProduct.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProduct.setHasFixedSize(true)
        productAdapter = ProductAdapter(requireContext(), productlist, this, this) { product, select ->
            optionSelect(product, select)
        }
        binding.rvProduct.adapter = productAdapter
        swipeToGesture(binding.rvProduct)
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
        builder.setTitle(Html.fromHtml("<font color='#F92391'>" + getString(R.string.attention) + "</font>"));

        //set message for alert dialog
        builder.setMessage(getString(R.string.want_delete_client) + " " + product.description)
        builder.setIcon(R.drawable.baseline_warning_24)

        //performing positive action
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            deleteProduct(product)
        }
//        //performing cancel action
//        builder.setNeutralButton("Cancel"){dialogInterface , which ->
//            Toast.makeText(applicationContext,"clicked cancel\n operation cancel",Toast.LENGTH_LONG).show()
//        }
        //performing negative action
        builder.setNegativeButton(getString(R.string.no)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun swipeToGesture(itemRv: RecyclerView?) {
        val swipeGesture = object : SwipeGesture(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                var actionBtnTapped = false
                try {
                    when (direction) {
                        ItemTouchHelper.LEFT -> {

                            val product = productlist[position]
                            productlist.removeAt(position)
                            productAdapter.notifyItemRemoved(position)

                            deleteProduct(product)
                            productAdapter.notifyDataSetChanged()

                            val snackBar = Snackbar.make(
                                binding.rvProduct, getString(R.string.item_deleted_client), 5000
                            ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                    super.onDismissed(transientBottomBar, event)
                                }

                                override fun onShown(transientBottomBar: Snackbar?) {
                                    transientBottomBar?.setAction(getString(R.string.undo_client)) {
//                                        clientlist.add(position, client)
                                        productlist.clear()
                                        insertProduct(product)
                                        getProducts()
//                                        clientAdapter.notifyItemInserted(position)
//                                        clientAdapter.notifyDataSetChanged()
                                        actionBtnTapped = true
                                    }
                                    super.onShown(transientBottomBar)
                                }
                            }).apply {
                                animationMode = Snackbar.ANIMATION_MODE_FADE
                            }
                            snackBar.setActionTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.pink,
                                )
                            )
                            snackBar.show()
                        }
                        ItemTouchHelper.RIGHT -> {
                            val clientPosition = productlist[position]
                            selectedProduct(clientPosition)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(itemRv)
    }

    // Filter  -----------------------------------------------------------
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

    // Firestore DataBase --------------------------------------------------
    private fun insertProduct(product: Product) {
        db!!.collection(dbProducts).document(product.barcode.toString())
            .set(product).addOnCompleteListener {
//                Util.exibirToast(requireContext(), getString(R.string.add_success_client))
            }.addOnFailureListener {
                Util.exibirToast(requireContext(), getString(R.string.error_save_client))
            }
    }

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

            } else {
                dialogProgress.dismiss()
                Util.exibirToast(requireContext(), getString(R.string.no_list_product))
            }
        }.addOnFailureListener { error ->
            dialogProgress.dismiss()
            Util.exibirToast(requireContext(), getString(R.string.error_show_product) + ":" + error.message.toString())
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

    private fun deleteProduct(product: Product) {
        val reference = db!!.collection(dbProducts)
        product.barcode?.let {
            reference.document(it).delete().addOnCompleteListener() { task ->
                if (task.isSuccessful) {
//                    removeImage(product.barcode!!)
//                    Util.exibirToast(requireContext(), getString(R.string.information_delete_product))
                    getProducts()
                } else {
                    Util.exibirToast(requireContext(), getString(R.string.error_delete_product) + ":" + task.exception.toString())
                }
            }
        }
    }

    private fun removeImage(barcode: String) {
        val reference = firebaseStorage.reference.child(dbProducts).child("${barcode}.jpg")
        reference.delete().addOnSuccessListener { task ->
        }.addOnFailureListener { error ->
            Util.exibirToast(requireContext(), getString(R.string.error_delete_image) + ":" + error.message.toString())
        }
    }
}