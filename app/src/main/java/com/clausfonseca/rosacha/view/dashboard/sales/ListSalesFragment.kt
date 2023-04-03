package com.clausfonseca.rosacha.view.dashboard.sales

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentSalesListBinding
import com.clausfonseca.rosacha.model.Product
import com.clausfonseca.rosacha.model.Sales
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.view.adapter.ProductAdapter
import com.clausfonseca.rosacha.view.adapter.SalesAdapter
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
        onBackPressed()
        getSales()
        initAdapter()
    }

    private fun initListeners() {
        binding.fabAddSales.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/fragment_sales_add")
            findNavController().navigate(uri)
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

    private fun getSales() {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        db!!.collection(dbSales).orderBy("salesDate").limit(10).get().addOnSuccessListener { results ->
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
                nextquery = db!!.collection(dbSales).orderBy("salesDate").startAfter(lastresult).limit(10)

                salesAdapter.notifyDataSetChanged()
//                initAdapter()
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

    }

    private fun initAdapter() {
        binding.rvSalesList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSalesList.setHasFixedSize(true)
        salesAdapter = SalesAdapter(requireContext(), salesList, this)
        binding.rvSalesList.adapter = salesAdapter
    }
}