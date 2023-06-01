package com.clausfonseca.rosacha.view.dashboard.sales

import android.annotation.SuppressLint
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
import com.clausfonseca.rosacha.databinding.FragmentDetailSalesBinding
import com.clausfonseca.rosacha.model.ItensSales
import com.clausfonseca.rosacha.model.Sales
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.pdf.MarksRecyclerAdapter
import com.clausfonseca.rosacha.utils.pdf.PDFConverter
import com.clausfonseca.rosacha.utils.pdf.PdfDetails
import com.clausfonseca.rosacha.view.adapter.ItemsSalesDetailAdapter
import com.google.firebase.firestore.FirebaseFirestore


class DetailSalesFragment : Fragment() {

    private lateinit var binding: FragmentDetailSalesBinding
    private var invoiceNumber: String = ""
    private val db = FirebaseFirestore.getInstance()
    private var dbSales: String = ""
    private var mySales: Sales? = null

    private val itemsSales = mutableListOf<ItensSales>()
    private lateinit var itemsSalesAdapter: ItemsSalesDetailAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        invoiceNumber = DetailSalesFragmentArgs.fromBundle(requireArguments()).id
        dbSales = getString(R.string.db_sales)
        initListeners()
        onBackPressed()
        selectSale(invoiceNumber)
        initAdapter()
    }

    private fun initListeners() {
        binding.btnBack.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/sales_fragment")
            findNavController().navigate(uri)
        }
        binding.btnReturn.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/sales_fragment")
            findNavController().navigate(uri)
        }

        binding.btnPdfDetail.setOnClickListener{
            createPdf()
        }
    }


    private fun initAdapter() {
        binding.rvSalesDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSalesDetail.setHasFixedSize(true)
        itemsSalesAdapter = ItemsSalesDetailAdapter(requireContext(), itemsSales)
        binding.rvSalesDetail.adapter = itemsSalesAdapter

    }

    @SuppressLint("SetTextI18n")
    private fun selectSale(id: String) {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")
        db.collection(dbSales).document(id).get()
            .addOnSuccessListener { item ->
                if (item != null && item.exists()) {

                    mySales = item.toObject(Sales::class.java)

                    binding.txtInvoiceDetail.text = id
                    binding.txtCustomerDetail.text = mySales?.client
                    binding.txtDateDetail.text = mySales?.salesDate
                    binding.txtSubTotalDetail.text = mySales?.price.toString()
                    binding.txtPaidDetail.text = mySales?.paid.toString()
                    binding.txtDiscountDetail.text = mySales?.discount.toString()
                    binding.txtTotalDetail.text = mySales?.totalPrice.toString()
                    binding.txtParceledDetail.text = "${mySales?.qtyParcel.toString()}X"
                    binding.txtParcelValueDetail.text = mySales?.parceled.toString()
                    binding.txtExpirationDetail.text = mySales?.parcelDate.toString()

                    itemsSalesAdapter.updateList(mySales?.itens!!)
                    dialogProgress.dismiss()

                } else {
                    Util.exibirToast(requireContext(), "Erro ao exibir venda")
                    dialogProgress.dismiss()
                }
            }.addOnFailureListener { error ->
                Util.exibirToast(requireContext(), "Erro ao exibir venda" + ":" + error.message.toString())
            }
    }


    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/sales_fragment")
                findNavController().navigate(uri)
            }
        })
    }

    private fun createPdf() {
        mySales?.let {
            val pdfDetails = PdfDetails(
                invoiceNumber,
                it,
            )
            val itemsSalesAdapter = MarksRecyclerAdapter(it.itens!!)
            val pdfConverter = PDFConverter()
            pdfConverter.createPdf(requireContext(), pdfDetails, requireActivity(), itemsSalesAdapter)
            invoiceNumber = ""
        }
    }
}