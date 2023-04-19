package com.clausfonseca.rosacha.view.dashboard.sales

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentSalesListBinding
import com.clausfonseca.rosacha.model.Sales
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.view.adapter.SalesAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class ListSalesFragment : Fragment(), SalesAdapter.LastItemRecyclerView {
    private lateinit var binding: FragmentSalesListBinding
    private lateinit var salesAdapter: SalesAdapter
    private lateinit var firebaseStorage: FirebaseStorage
    private var dbSales: String = ""
    private val salesList = mutableListOf<Sales>()
    private var db: FirebaseFirestore? = null
    private var nextquery: Query? = null
    private var isFilterOn = false
    private var dbFilter = "client"
    private var selecteDItemIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSalesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        dbSales = getString(R.string.db_sales)
        initListeners()
        initAdapter()
        getSales()
        searchSales()
        onBackPressed()
    }

    override fun setSingleChoiceItems(filter: Array<String>, selecteDItemIndex: Int, any: Any): Any {
        TODO("Not yet implemented")
    }

    private fun initListeners() {

        binding.fabAddSales.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/fragment_sales_add")
            findNavController().navigate(uri)
        }

        binding.btnMenuSales.setOnClickListener {
            showConformationIndexDialog()
        }
    }

    override fun lastItemRecyclerView(isShow: Boolean) {
        if (isFilterOn)
        else getMoreSales()
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/home_fragment")
                findNavController().navigate(uri)
            }
        })
    }

    private fun initAdapter() {
        binding.rvSalesList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSalesList.setHasFixedSize(true)
        salesAdapter = SalesAdapter(requireContext(), salesList, this)
        binding.rvSalesList.adapter = salesAdapter
    }

    private fun getSales() {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        db!!.collection(dbSales).orderBy("salesDate", Query.Direction.DESCENDING).limit(10).get()
            .addOnSuccessListener { results ->
                dialogProgress.dismiss()

                if (results.size() > 0) {
                    salesList.clear()

                    // result é uma lista
                    for (result in results) {
                        val sales = result.toObject(Sales::class.java)
                        salesList.add(sales)
                    }
                    // pegar ultimo item da query
                    val lastresult = results.documents[results.size() - 1]
                    nextquery =
                        db!!.collection(dbSales).orderBy("salesDate", Query.Direction.DESCENDING).startAfter(lastresult).limit(10)

                    salesAdapter.notifyDataSetChanged()

                } else {
                    dialogProgress.dismiss()
                    Util.exibirToast(requireContext(), "Nao existem vendas para serem exibidas")
                }
            }.addOnFailureListener { error ->
                dialogProgress.dismiss()
                Util.exibirToast(requireContext(), "erro ao exibir uma venda" + ":" + error.message.toString())
            }
    }

    private fun getMoreSales() {

        nextquery?.get()?.addOnSuccessListener { results ->

            // o if e para verificar se chegou o fim da lista
            if (results.size() > 0) {
                // pegar ultimo item da query
                val lastresult = results.documents[results.size() - 1]

                nextquery =
                    db!!.collection(dbSales).orderBy("salesDate", Query.Direction.DESCENDING).startAfter(lastresult).limit(10)

                for (result in results) {
                    val sales = result.toObject(Sales::class.java)
                    salesList.add(sales)
                }
                // notificar que teve atualizalçao
                salesAdapter.notifyDataSetChanged()
            } else {
//                Util.exibirToast(requireContext(), "Não ha mais itens para serem exibidos")
            }
        }?.addOnFailureListener() { error ->
            Util.exibirToast(requireContext(), error.message.toString())
        }
    }

    private fun searchSales() {
        binding.svSales.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS + InputType.TYPE_CLASS_TEXT
        binding.svSales.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                isFilterOn = true
                filterSearchSales(newText.toString())
                return true
            }
        })
        binding.svSales.setOnCloseListener(object : SearchView.OnCloseListener,
            android.widget.SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                binding.svSales.onActionViewCollapsed()
                salesList.clear()
                salesAdapter.notifyDataSetChanged()
                getSales()
                isFilterOn = false
                return true
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterSearchSales(newText: String) {
        var newDouble: Any

        if (dbFilter == "totalPrice") {

            if (!newText.isNullOrEmpty()) {
                newDouble = newText.toDouble()
                db!!.collection(dbSales).whereEqualTo("totalPrice", newDouble).limit(30).get().addOnSuccessListener { results ->
                    if (results.size() > 0) {
                        salesList.clear()
                        for (result in results) {
                            val sales = result.toObject(Sales::class.java)
                            salesList.add(sales)
                        }
                        salesAdapter.notifyDataSetChanged()
                    }
                }?.addOnFailureListener { error ->
                    Toast.makeText(
                        requireContext(),
                        "Error ${error.message.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            newDouble = newText
            db!!.collection(dbSales).orderBy(dbFilter).startAt(newDouble)
                .endAt(newText + "\uf8ff").limit(30).get().addOnSuccessListener { results ->
                    if (results.size() > 0) {
                        salesList.clear()
                        for (result in results) {
                            val sales = result.toObject(Sales::class.java)
                            salesList.add(sales)
                        }
                        salesAdapter.notifyDataSetChanged()
                    }
                }?.addOnFailureListener { error ->
                    Toast.makeText(
                        requireContext(),
                        "Error ${error.message.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun showConformationIndexDialog() {
        val filter = arrayOf("Client", "Date", "Id", "Price")

        var selectedFilter = filter[selecteDItemIndex]
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(Html.fromHtml("<font color='#F92391'>" + "Selecione o Filtro" + "</font>"))
            .setSingleChoiceItems(filter, selecteDItemIndex) { dialog, witch ->
                selecteDItemIndex = witch
                selectedFilter = filter[witch]
            }
            .setPositiveButton("OK") { _, _ ->
                binding.svSales.setQuery("", false);
                when (selectedFilter) {
                    "Date" -> {
                        dbFilter = "salesDate"
                        binding.svSales.queryHint = "filter by Date"
                        binding.svSales.inputType = InputType.TYPE_CLASS_DATETIME
                    }
                    "Id" -> {
                        dbFilter = "id"
                        binding.svSales.queryHint = "filter by ID"
                        binding.svSales.inputType = InputType.TYPE_CLASS_NUMBER
                    }
                    "Price" -> {
                        dbFilter = "totalPrice"
                        binding.svSales.queryHint = "filter by Total Price"
                        binding.svSales.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL + InputType.TYPE_CLASS_NUMBER
                    }
                    else -> {
                        dbFilter = "client"
                        binding.svSales.queryHint = "filter by Client"
                        binding.svSales.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS + InputType.TYPE_CLASS_TEXT
                    }
                }
            }
            .setNeutralButton("Cancel") { _, _ ->

            }
            .show()
    }


}