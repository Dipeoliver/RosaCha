package com.clausfonseca.rosacha.view.dashboard.sales

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetAfterSalesBinding
import com.google.android.material.bottomsheet.BottomSheetDialog


class AfterSalesFragment : Fragment() {
    private var dialogAfterSales: BottomSheetDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_after_sales, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBackPressed()
        showBottomSheetDialogAfterSales()
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
//            createPdf()
            dialogAfterSales?.dismiss()
        }
        dialogAfterSales?.setContentView(sheetBinding.root)
        dialogAfterSales?.setCancelable(false)
        dialogAfterSales?.show()
    }
}