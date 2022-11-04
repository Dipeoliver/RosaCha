package com.clausfonseca.rosacha.view.dashboard.product

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentAddProductBinding
import com.clausfonseca.rosacha.view.helper.FirebaseHelper
import com.clausfonseca.rosacha.view.model.AddProduct
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.text.SimpleDateFormat
import java.util.*

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var addProduct: AddProduct
    private var newTask: Boolean = true
    private var statusOwner: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureButton()
        initListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                binding.edtBarcodeProduct.setText(result.contents)
                binding.edtReferenceProduct.requestFocus()

            } else {
                binding.edtBarcodeProduct.setText("scan failed")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            binding.edtBarcodeProduct.requestFocus()
        }
    }

    @Suppress("DEPRECATION")
    private fun configureButton() {
        binding.btnScan.setOnClickListener {
            val integrator: IntentIntegrator =
                IntentIntegrator.forSupportFragment(this@AddProductFragment)
            integrator.setPrompt("Scanner RosaCha Ativo")
            integrator.initiateScan()
        }
    }

    private fun initListeners() {
        binding.btnAddProduct.setOnClickListener { validateData() }
        binding.rgOwnerProduct.setOnCheckedChangeListener() { _, id ->
            statusOwner = when (id) {
                R.id.claudia -> 0
                else -> 1
            }
        }
    }

    private fun validateData() {

        val barcode = binding.edtBarcodeProduct.text.toString().trim()
        val reference = binding.edtReferenceProduct.text.toString().trim()
        val description = binding.edtDescriptionProduct.text.toString().trim()
        val brand = binding.edtBrandProduct.text.toString().trim()
        val provider = binding.edtProviderProduct.text.toString().trim()
        val size = binding.edtSizeProduct.text.toString().trim()
        val color = binding.edtColorProduct.text.toString().trim()
        val costPrice = binding.edtCostProduct.text.toString().trim()
        val salesPrice = binding.edtSalesProduct.text.toString().trim()

        if (barcode.isNotEmpty() && description.isNotEmpty() && size.isNotEmpty() &&
            color.isNotEmpty() && costPrice.isNotEmpty() && salesPrice.isNotEmpty()
        ) {
            binding.progressBar4.isVisible = true

            if (newTask) addProduct = AddProduct()

            val date = Calendar.getInstance().time
            var dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val productDate = dateTimeFormat.format(date)

            addProduct.barcode = barcode
            addProduct.reference = reference
            addProduct.description = description
            addProduct.brand = brand
            addProduct.provider = provider
            addProduct.size = size
            addProduct.color = color
            addProduct.cost_price = costPrice.toDouble()
            addProduct.sales_price = salesPrice.toDouble()
            addProduct.owner = statusOwner
            addProduct.productDate = productDate
            insertProduct()
        } else {
            Toast.makeText(
                requireContext(),
                "Preencher os campos Obrigatórios",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun insertProduct() {
        FirebaseHelper
            .getDatabase()
            .child("addProduct")
            .child(FirebaseHelper.getIdUser() ?: "") // id do usuario
            .child(addProduct.id)
            .setValue(addProduct)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (newTask) { // nova tarefa
//                        findNavController().popBackStack() // voltar para a tela anterior
                        Toast.makeText(
                            requireContext(),
                            "Tarefa Salva com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.progressBar4.isVisible = false
                    } else { // iditando tarefa
                        binding.progressBar4.isVisible = false
                        Toast.makeText(
                            requireContext(),
                            "Tarefa Atualizada com Sucesso",
                            Toast.LENGTH_SHORT
                        )
                    }

                } else {
                    Toast.makeText(requireContext(), "Erro ao salvar Tarefa", Toast.LENGTH_SHORT)
                        .show()

                }
            }.addOnFailureListener {
                binding.progressBar4.isVisible = false
                Toast.makeText(requireContext(), "Erro ao salvar Tarefa", Toast.LENGTH_SHORT).show()
            }
    }
}