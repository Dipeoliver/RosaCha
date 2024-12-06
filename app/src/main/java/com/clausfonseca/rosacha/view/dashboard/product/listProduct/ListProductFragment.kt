package com.clausfonseca.rosacha.view.dashboard.product.listProduct

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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentProductListBinding
import com.clausfonseca.rosacha.model.ProductModel
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Swipe.SwipeGesture
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.extencionFunctions.getDbProduct
import com.clausfonseca.rosacha.view.adapter.ProductAdapter
import com.clausfonseca.rosacha.view.common.CommonModelState
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListProductFragment : Fragment(), ProductAdapter.LastItemRecyclerView {

    private lateinit var binding: FragmentProductListBinding
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<ProductModel>()
    private val viewModel: ListProductViewModel by viewModels()
    private val dialogProgress = DialogProgress()
    var nextquery: Query? = null
    var isFilterOn = false
    var product = ProductModel()
    var actionBtnTapped = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        initAdapter()
        viewModel.getProducts(getDbProduct(requireContext()), productList)
        searchProduct()
        onBackPressed()
        configureObservables()
    }

    override fun lastItemRecyclerView(isShow: Boolean) {
//        if (isFilterOn)
//        else getMoreProducts()
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

    private fun selectedProduct(productModel: ProductModel) {
//        findNavController().navigate(


//            ProductFragmentDirections.actionProductFragmentToEditProductFragment(
//                productModel
//            )
//        )
    }

    private fun initAdapter() {
        binding.rvProduct.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProduct.setHasFixedSize(true)
        productAdapter = ProductAdapter(requireContext(), productList, this, this) { product, select ->

        }
        binding.rvProduct.adapter = productAdapter
        swipeToGesture(binding.rvProduct)
    }

    private fun swipeToGesture(itemRv: RecyclerView?) {
        val swipeGesture = object : SwipeGesture(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                try {
                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            viewModel.removeProduct(
                                dbProduct = getDbProduct(requireContext()),
                                productModel = productList[position],
                                position = position
                            )

                        }

                        ItemTouchHelper.RIGHT -> {
                            val productPosition = productList[position]
                            selectedProduct(productPosition)
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

    private fun configureObservables() {
        viewModel.model.screenState.observe(viewLifecycleOwner) {
            handleState(it)
        }
    }

    private fun handleState(state: CommonModelState.CommonState?) {
        when (state) {
            is CommonModelState.CommonState.Loading -> {
                if (state.isLoading) dialogProgress.show(childFragmentManager, "0")
                else dialogProgress.dismiss()
            }

            is CommonModelState.CommonState.RemoveProductSuccess -> {
                product = productList[state.position]
                productList.removeAt(state.position)
                productAdapter.notifyItemRemoved(state.position)


                val snackBar = Snackbar.make(
                    binding.rvProduct, getString(R.string.item_deleted_client), 5000
                ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {
                        transientBottomBar?.setAction(getString(R.string.undo_client)) {
                            productList.clear()
                            viewModel.insertProduct(getDbProduct(requireContext()), product)
                            viewModel.getProducts(getDbProduct(requireContext()), productList)
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

                viewModel.removeImageFireStorage(getDbProduct(requireContext()), product.barcode.toString())
                viewModel.getProducts(getDbProduct(requireContext()), productList)


            }

            is CommonModelState.CommonState.DeleteProductError -> {
                Util.exibirToast(
                    requireContext(),
                    getString(R.string.error_delete_product) + ":" + state.message
                )
            }

            is CommonModelState.CommonState.InsertProductSuccess -> {
                Util.exibirToast(requireContext(), getString(R.string.error_save_product))
            }

            is CommonModelState.CommonState.GetProductsLoaded, CommonModelState.CommonState.FilterProductSuccess -> {
                productList.clear()
                productList.addAll(viewModel.model.productsResult)
                productAdapter.notifyDataSetChanged()
            }

            else -> {
            }
        }
    }
//    private fun optionSelect(productModel: ProductModel, select: Int) {
//        when (select) {
//            ProductAdapter.SELECT_REMOVE -> {
//                configDialog(productModel)
//            }
//
//            ProductAdapter.SELECT_EDIT -> {
//            }
//        }
//    }

    private fun configDialog(productModel: ProductModel) {

        val builder = AlertDialog.Builder(requireContext())

        //set title for alert dialog
//        builder.setTitle("Atenção")
        builder.setTitle(Html.fromHtml("<font color='#F92391'>" + getString(R.string.attention) + "</font>"));

        //set message for alert dialog
        builder.setMessage(getString(R.string.want_delete_client) + " " + productModel.description)
        builder.setIcon(R.drawable.baseline_warning_24)

        //performing positive action
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
//            deleteProduct(productModel)
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
                viewModel.filterSearchProducts(getDbProduct(requireContext()), newText ?: "", productList)
                Log.d("Diego-onQueryTextChange", newText.toString())
                return true
            }
        })
        binding.svProduct.setOnCloseListener(object : SearchView.OnCloseListener,
            android.widget.SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                binding.svProduct.onActionViewCollapsed()
                productList.clear()
                productAdapter.notifyDataSetChanged()
                viewModel.getProducts(getDbProduct(requireContext()), productList)
                isFilterOn = false
                return true
            }
        })
    }

//    private fun filterSearchProduct(newText: String) {
//        db!!.collection(dbProducts).orderBy("description").startAt(newText)
//            .endAt(newText + "\uf8ff")?.limit(5)?.get()?.addOnSuccessListener { results ->
//                if (results.size() > 0) {
//                    productlist.clear()
//                    for (result in results) {
//                        val productModel = result.toObject(ProductModel::class.java)
//                        productlist.add(productModel)
//                    }
//                    productAdapter.notifyDataSetChanged()
//                }
//            }?.addOnFailureListener { error ->
//                Toast.makeText(
//                    requireContext(),
//                    "Error ${error.message.toString()}",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//    }

//    private fun getProducts() {
//        val dialogProgress = DialogProgress()
//        dialogProgress.show(childFragmentManager, "0")
//
//        db!!.collection(dbProducts).orderBy("description").limit(10).get().addOnSuccessListener { results ->
//            dialogProgress.dismiss()
//
//
//            if (results.size() > 0) {
//                productlist.clear()
//
//                // result é uma lista
//                for (result in results) {
//                    val productModel = result.toObject(ProductModel::class.java)
//                    productlist.add(productModel)
//                }
//                // pegar ultimo item da query
//                val lastresult = results.documents[results.size() - 1]
//                nextquery = db!!.collection(dbProducts)
//                    .orderBy("description")
//                    .startAfter(lastresult)
//                    .limit(10)
//                Log.d("nextQuery", "${nextquery}")
//                productAdapter.notifyDataSetChanged()
//
//            } else {
//                dialogProgress.dismiss()
//                Util.exibirToast(requireContext(), getString(R.string.no_list_product))
//            }
//        }.addOnFailureListener { error ->
//            dialogProgress.dismiss()
//            Util.exibirToast(requireContext(), getString(R.string.error_show_product) + ":" + error.message.toString())
//        }
//    }

//    private fun getMoreProducts() {
//        nextquery?.get()?.addOnSuccessListener { results ->
//            Log.d("****nextqueryProduct", "${(nextquery?.get()?.addOnSuccessListener {})}")
//            // o if e para verificar se chegou o fim da lista
//            if (results.size() > 0) {
//                // pegar ultimo item da query
//                val lastresult = results.documents[results.size() - 1]
//                Log.d("DIEGO", "$lastresult")
//                nextquery = db!!.collection(dbProducts).orderBy("description").startAfter(lastresult).limit(10)
//
//                for (result in results) {
//                    val productModel = result.toObject(ProductModel::class.java)
//                    productlist.add(productModel)
//                }
//                // notificar que teve atualizalçao
//                productAdapter.notifyDataSetChanged()
//            } else {
////                Util.exibirToast(requireContext(), "Não ha mais itens para serem exibidos")
//            }
//        }?.addOnFailureListener() { error ->
//            Util.exibirToast(requireContext(), error.message.toString())
//        }
//    }

//    private fun deleteProduct(productModel: ProductModel) {
//        val reference = db!!.collection(dbProducts)
//        productModel.barcode?.let {
//            reference.document(it).delete().addOnCompleteListener() { task ->
//                if (task.isSuccessful) {
////                    removeImage(productModel.barcode!!)
////                    Util.exibirToast(requireContext(), getString(R.string.information_delete_product))
//                    getProducts()
//                } else {
//                    Util.exibirToast(requireContext(), getString(R.string.error_delete_product) + ":" + task.exception.toString())
//                }
//            }
//        }
//    }

//    private fun removeImage(barcode: String) {
//        val reference = firebaseStorage.reference.child(dbProducts).child("${barcode}.jpg")
//        reference.delete().addOnSuccessListener { task ->
//        }.addOnFailureListener { error ->
//            Util.exibirToast(requireContext(), getString(R.string.error_delete_image) + ":" + error.message.toString())
//        }
//    }
}