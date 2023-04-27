package com.clausfonseca.rosacha.view.dashboard.sales

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentAfterSalesBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetAfterSalesBinding
import com.clausfonseca.rosacha.model.Sales
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.pdf.MarksRecyclerAdapter
import com.clausfonseca.rosacha.utils.pdf.PDFConverter
import com.clausfonseca.rosacha.utils.pdf.PdfDetails
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore

class AfterSalesFragment : Fragment() {
    private lateinit var binding: FragmentAfterSalesBinding
    private var dialogAfterSales: BottomSheetDialog? = null
    private val db = FirebaseFirestore.getInstance()
    private var dbSales: String = ""
    private var mySales: Sales? = null
    private var invoiceNumber: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAfterSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // recebendo parametro atraves Bundle
        invoiceNumber = requireArguments().getString("id").toString()
        dbSales = getString(R.string.db_sales)
        onBackPressed()
        showBottomSheetDialogAfterSales()
    }

    override fun onResume() {
        super.onResume()
        if (invoiceNumber == "") {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/home_fragment")
            findNavController().navigate(uri)
        }
    }


    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val uri = Uri.parse("android-app://com.clausfonseca.rosacha/sales_fragment")
                    findNavController().navigate(uri)
                }
            })
    }

    private fun showBottomSheetDialogAfterSales() {
        dialogAfterSales = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)

        val sheetBinding: ItemCustomBottonSheetAfterSalesBinding =
            ItemCustomBottonSheetAfterSalesBinding.inflate(layoutInflater, null, false)

        sheetBinding.clNewSales.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/fragment_sales_add")
            findNavController().navigate(uri)
            dialogAfterSales?.dismiss()

        }

        sheetBinding.clListSales.setOnClickListener {
            // link para lista de vendas
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/home_fragment")
            findNavController().navigate(uri)
            dialogAfterSales?.dismiss()
        }
        sheetBinding.clShareSales.setOnClickListener {
            getSalesData()
            dialogAfterSales?.dismiss()
        }
        dialogAfterSales?.setContentView(sheetBinding.root)
        dialogAfterSales?.setCancelable(false)
        dialogAfterSales?.show()
    }

    private fun getSalesData() {

        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")
        db.collection(dbSales).document(invoiceNumber).get()
            .addOnSuccessListener { sales ->
                if (sales != null && sales.exists()) {
                    mySales = sales.toObject(Sales::class.java)
                    dialogProgress.dismiss()
                    createPdf()
                } else {
                    Util.exibirToast(requireContext(), getString(R.string.error_pdf))
                    dialogProgress.dismiss()
                }
            }.addOnFailureListener { error ->
                Util.exibirToast(requireContext(), getString(R.string.error_pdf) + ":" + error.message.toString())
            }
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