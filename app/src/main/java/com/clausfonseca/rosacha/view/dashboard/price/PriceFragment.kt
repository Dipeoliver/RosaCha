package com.clausfonseca.rosacha.view.dashboard.price

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.clausfonseca.rosacha.databinding.FragmentPriceBinding
import com.clausfonseca.rosacha.view.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class PriceFragment : Fragment() {

    private var _binding: FragmentPriceBinding? = null
    private val binding get() = _binding!!
    private lateinit var product: Product
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPriceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iniclicks()
    }

    private fun iniclicks() {
        binding.btnSearchPrice.setOnClickListener {

//            getPrice(binding.edtBarcodePrice.text)
            selectPrice(binding.edtBarcodePrice.text)
        }
    }


    private fun selectPrice(barcode: Editable) {
        db.collection("Products").document(barcode.toString())
            .addSnapshotListener { documento, error ->
                if (documento != null) {
                    val price = documento.getLong("sales_price")
                    binding.edtDescriptionPrice.setText(documento.getString("description"))
                    binding.txtValuePrice.text = price.toString()
                } else
                    Toast.makeText(
                        requireContext(),
                        "Produto não encontrado",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        // arrumar quando der erro
    }

    private fun getPrice(barcode: Editable) {
        db.collection("Product")
            .whereEqualTo("barcode", "2")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Toast.makeText(
                        requireContext(),
                        "Produto encontrado",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.edtDescriptionPrice.setText(document.data["description"].toString())
                    binding.txtValuePrice.text = (document.data["price"].toString())
                }
            }
            .addOnFailureListener { exception ->
//                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}