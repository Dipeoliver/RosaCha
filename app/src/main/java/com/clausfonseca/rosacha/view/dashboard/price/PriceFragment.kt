package com.clausfonseca.rosacha.view.dashboard.price

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.br.jafapps.bdfirestore.util.DialogProgress
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentPriceBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat

class PriceFragment : Fragment() {

    private lateinit var binding: FragmentPriceBinding

    //    private lateinit var product: Product
    private val db = FirebaseFirestore.getInstance()
    private val price: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPriceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureButton()
        iniclicks()
    }

    private fun iniclicks() {
        binding.edtBarcodePrice.requestFocus()
        binding.btnSearchPrice.setOnClickListener {
            selectPrice(binding.edtBarcodePrice.text.toString())
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                val Finalresult = result.contents
                binding.edtBarcodePrice.setText(Finalresult)
                binding.edtBarcodePrice.requestFocus()
                binding.edtBarcodePrice.selectAll()
                selectPrice(Finalresult)
            } else {
                binding.edtBarcodePrice.setText("scan failed")
                cleaner()
                binding.edtBarcodePrice.requestFocus()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            binding.edtBarcodePrice.requestFocus()
            cleaner()
            binding.edtBarcodePrice.requestFocus()
        }
    }

    @Suppress("DEPRECATION")
    private fun configureButton() {
        binding.btnScanPrice.setOnClickListener {
            val integrator: IntentIntegrator =
                IntentIntegrator.forSupportFragment(this@PriceFragment)
            integrator.setPrompt("Scanner RosaCha Ativo")
            integrator.initiateScan()
        }
    }

    private fun selectPrice(barcode: String) {

        if (barcode != null && barcode.isNotEmpty()) {
            val dialogProgress = DialogProgress()
            dialogProgress.show(childFragmentManager, "0")

            db.collection("Products").document(barcode.toString()).get()
                .addOnSuccessListener { product ->

                    dialogProgress.dismiss()

                    if (product != null && product.exists()) {
                        val price: Double = product.getDouble("sales_price") ?: 0.0
                        binding.edtDescriptionPrice.setText(product.getString("description"))
                        binding.edtColorPrice.setText(product.getString("color"))
                        binding.edtSizePrice.setText(product.getString("size"))

                        val df = DecimalFormat("#.##")
                        df.roundingMode = RoundingMode.UP

                        binding.txtValuePrice.text = (df.format(price)).toString()
                        binding.txtValue2xPrice.text = (df.format(price / 2)).toString()
                        binding.txtValue3xPrice.text = (df.format(price / 3)).toString()
                        binding.txtValue4xPrice.text = (df.format(price / 4)).toString()
                        binding.txtValue5xPrice.text = (df.format(price / 5)).toString()
                        binding.txtValue6xPrice.text = (df.format(price / 6)).toString()


                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Erro ao exibir o produto, ele não existe",
                            Toast.LENGTH_LONG
                        ).show()
                        cleaner()
                    }
                }.addOnFailureListener { error ->
                    dialogProgress.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Erro de comunicação com servidor ${error.message.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(
                requireContext(),
                "Campo Barcode não pode estar vazio",
                Toast.LENGTH_SHORT
            ).show()
            binding.edtBarcodePrice.requestFocus()

        }

    }

    private fun cleaner() {
        binding.edtDescriptionPrice.setText("")
        binding.txtValuePrice.text = "00.00"
        binding.txtValue2xPrice.text = "00.00"
        binding.txtValue3xPrice.text = "00.00"
        binding.txtValue4xPrice.text = "00.00"
        binding.txtValue5xPrice.text = "00.00"
        binding.txtValue6xPrice.text = "00.00"
        binding.edtBarcodePrice.requestFocus()
        binding.edtBarcodePrice.selectAll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}