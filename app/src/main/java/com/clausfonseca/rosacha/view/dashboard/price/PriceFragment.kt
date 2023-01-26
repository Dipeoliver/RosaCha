package com.clausfonseca.rosacha.view.dashboard.price

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.br.jafapps.bdfirestore.util.DialogProgress
import com.br.jafapps.bdfirestore.util.Util
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentPriceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.math.RoundingMode
import java.text.DecimalFormat

class PriceFragment : Fragment() {

    private lateinit var binding: FragmentPriceBinding
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPriceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        firebaseStorage = Firebase.storage
        configureButton()
        iniclicks()
    }

    private fun iniclicks() {
        binding.edtBarcodePrice.requestFocus()
        binding.btnSearchPrice.setOnClickListener {
            selectPrice(binding.edtBarcodePrice.text.toString())
        }
    }

    // BARCODE----------------------------------------------------------------------------
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
    // ----------------------------------------------------------------------------------

    // FIRESTORE-------------------------------------------------------------------------

    private fun selectPrice(barcode: String) {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")
        if (barcode != null && barcode.isNotEmpty()) {
            db.collection("Products").document(barcode.toString()).get()
                .addOnSuccessListener { product ->
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

                        download_Image_Name(barcode)
                        dialogProgress.dismiss()

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Erro ao exibir o produto, ele não existe",
                            Toast.LENGTH_LONG
                        ).show()
                        dialogProgress.dismiss()
                        cleaner()
                    }
                }.addOnFailureListener { error ->
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
            dialogProgress.dismiss()
            binding.edtBarcodePrice.requestFocus()
        }
    }
    // ----------------------------------------------------------------------------------

    // STORAGE---------------------------------------------------------------------------
    fun download_Image_Name(barcode: String) {
        val reference = firebaseStorage.reference.child("Products").child(barcode + ".jpg")
//        val reference =
//            firebaseStorage!!.reference   // download de imagem adicionando usuario especifico
//                .child("imagens")
//                .child(uid.toString())
//                .child("uploadImagem.jpg")
        reference.downloadUrl.addOnSuccessListener { task ->
            val urlImage = task
            Glide.with(requireContext()).asBitmap().load(urlImage)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Util.exibirToast(requireContext(), "Erro ao carregar a imagem: ${e.toString()}")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
//                        Util.exibirToast(requireContext(), "Imagem carregada com Sucesso")
                        return false
                    }
                }).into(binding.imvProduct)

        }.addOnFailureListener { error ->
            Util.exibirToast(requireContext(), "Erro ao carregar imagem2 ${error.message.toString()}")
        }
    }

    // ----------------------------------------------------------------------------------
    private fun cleaner() {
        binding.edtDescriptionPrice.setText("")
        binding.txtValuePrice.text = ""
        binding.txtValue2xPrice.text = ""
        binding.txtValue3xPrice.text = ""
        binding.txtValue4xPrice.text = ""
        binding.txtValue5xPrice.text = ""
        binding.txtValue6xPrice.text = ""
        binding.edtSizePrice.setText("")
        binding.edtColorPrice.setText("")
        binding.edtBarcodePrice.requestFocus()
        binding.edtBarcodePrice.selectAll()
        binding.imvProduct.setImageResource(R.drawable.no_image)

    }
}