package com.clausfonseca.rosacha.view.dashboard.product

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.br.jafapps.bdfirestore.util.DialogProgress
import com.br.jafapps.bdfirestore.util.Util
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentProductAddBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetBinding
import com.clausfonseca.rosacha.model.Product
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class AddProductFragment : Fragment() {

    private lateinit var binding: FragmentProductAddBinding

    private lateinit var firebaseStorage: FirebaseStorage
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    var uri_Imagem: Uri? = null
    private var pictureName: String? = ""

    private lateinit var product: Product

    private var newTask: Boolean = true
    private var statusOwner: Int = 0
    private var owner: String = ""

    val dialogProgress = DialogProgress()
    var dialog: BottomSheetDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureButton()
        initListeners()
        auth = Firebase.auth
        firebaseStorage = Firebase.storage
    }

    // BARCODE  &&  IMAGEVIEW   --------------------------------------------------------
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // setar a imagem na ImageView

        if (requestCode == 11 || requestCode == 22) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
                binding.imvPlus.visibility = GONE

                if (requestCode == 11 && data != null) {  // galeria
                    uri_Imagem = data.data

                    binding.imvPhoto.setImageURI(uri_Imagem)

                } else if (requestCode == 22 && uri_Imagem != null) {// camera

                    binding.imvPhoto.setImageURI(uri_Imagem)
                }
                dialog?.dismiss()
            }
        } else {
            // BARCODE
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
    }

    // BARCODE
    @Suppress("DEPRECATION")
    private fun configureButton() {
        binding.btnScan.setOnClickListener {
            val integrator: IntentIntegrator =
                IntentIntegrator.forSupportFragment(this@AddProductFragment)
            integrator.setPrompt("Scanner RosaCha Ativo")
            integrator.initiateScan()
        }
    }
    // ----------------------------------------------------------------------------------

    // STORAGE----------------------------------------------------------------------------
    //  Capturar imagem da Camera
    private fun obterImagemdaCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // versão nova
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            val resolver = activity?.contentResolver
            uri_Imagem =
                resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        } else { // versão antiga
            val autorização = "com.clausfonseca.rosacha"
            val diretorio =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val path = diretorio.path ?: ""
            val nomeImagem = path + "/Products" + pictureName + ".jpg"
            if (nomeImagem == "/Products.jpg") {
                val nomeImagem = diretorio.path + "/Products" + System.currentTimeMillis() + ".jpg"
            }
            val file = File(nomeImagem)
            uri_Imagem = activity?.let { FileProvider.getUriForFile(it.baseContext, autorização, file) }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri_Imagem)
        startActivityForResult(intent, 22)
    }

    // selecionar imagem da galeria
    private fun obterImagemdaGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, "Escolha uma Imagem"), 11)
    }

    // com recurso para diminuir a imagem
    fun uploadImagem() {

        pictureName = binding.edtBarcodeProduct.text.toString()

        activity?.let {
            Glide.with(it.baseContext).asBitmap().load(uri_Imagem)
                .apply(RequestOptions.overrideOf(800, 480)).listener(object : RequestListener<Bitmap> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Util.exibirToast(requireContext(), "Erro ao diminuir imagem")
                        return false
                    }

                    override fun onResourceReady(
                        bitmap: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {

                        val baos = ByteArrayOutputStream()
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                        val data = baos.toByteArray()
//                        val uid = auth.currentUser?.uid

                        val reference =
                            firebaseStorage.reference   // upload de imagem adicionando usuario especifico
                                .child("Products")
//                                .child(uid.toString())
                                .child(pictureName + ".jpg")
                        val uploadTask = reference.putBytes(data)
                        uploadTask.addOnSuccessListener {

                        }.addOnFailureListener { error ->
                            Util.exibirToast(
                                requireContext(),
                                "Erro ao realizar o upload da imagem: ${error.message.toString()}"
                            )
                        }
                        return false
                    }
                }).submit()
        }
    }
    // ----------------------------------------------------------------------------------

    // FIRESTORE--------------------------------------------------------------------------
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

        if (barcode.isNotEmpty() && description.isNotEmpty() && size.isNotEmpty() && costPrice.isNotEmpty() && salesPrice.isNotEmpty()
        ) {


            dialogProgress.show(childFragmentManager, "0")

            if (newTask) product = Product()

            val date = Calendar.getInstance().time
            val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val productDate = dateTimeFormat.format(date)

            product.barcode = barcode
            product.reference = reference
            product.description = description.uppercase()
            product.brand = brand.uppercase()
            product.provider = provider.uppercase()
            product.size = size
            product.color = color.uppercase()
            product.cost_price = costPrice.toDouble()
            product.sales_price = salesPrice.toDouble()
            owner = if (statusOwner == 0) {
                "Claudia"
            } else {
                "Claudenice"
            }
            product.owner = owner
            product.productDate = productDate
            insertProduct()
            if (uri_Imagem != null) {
                uploadImagem()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Preencher os campos Obrigatórios",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Inserir produto no Firestore
    private fun insertProduct() {
        db.collection("Products").document(product.barcode)
            .set(product).addOnCompleteListener {
                Toast.makeText(
                    requireContext(),
                    "Produto adicionado com sucesso",
                    Toast.LENGTH_SHORT
                ).show()
                cleaner()
                dialogProgress.dismiss()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao salvar Produto", Toast.LENGTH_SHORT)
                    .show()
            }
    }
    // ----------------------------------------------------------------------------------

    private fun initListeners() {
        binding.btnAddProduct.setOnClickListener {
            validateData()
        }
        binding.rgOwnerProduct.setOnCheckedChangeListener() { _, id ->
            statusOwner = when (id) {
                R.id.claudia -> 0
                else -> 1
            }
        }

        binding.btnBack.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/product_fragment")
            findNavController().navigate(uri)
        }

        binding.imvPhoto.setOnClickListener {
            if (binding.edtBarcodeProduct.text.isNotEmpty()) showBottomSheetDialog()
            else Util.exibirToast(requireContext(), "Preencher campo Barcode Primeiro")
        }
    }

    private fun showBottomSheetDialog() {
        dialog = BottomSheetDialog(requireContext())

        val sheetBinding: ItemCustomBottonSheetBinding = ItemCustomBottonSheetBinding.inflate(layoutInflater, null, false)

        sheetBinding.imvBottomPhoto.setOnClickListener {
            obterImagemdaCamera()
        }
        sheetBinding.txtBottomPhoto.setOnClickListener {
            obterImagemdaCamera()
        }

        sheetBinding.imvBottomGallery.setOnClickListener {
            obterImagemdaGaleria()
        }
        sheetBinding.txtBottomGallery.setOnClickListener {
            obterImagemdaGaleria()
        }
        dialog?.setContentView(sheetBinding.root)
        dialog?.show()
    }

    private fun cleaner() {
        binding.apply {
            edtBarcodeProduct.text.clear()
            edtReferenceProduct.text.clear()
            edtDescriptionProduct.text.clear()
            edtBrandProduct.text.clear()
            edtProviderProduct.text.clear()
            edtSizeProduct.text.clear()
            edtColorProduct.text.clear()
            edtCostProduct.text.clear()
            edtSalesProduct.text.clear()
            edtBarcodeProduct.requestFocus()
            binding.imvPhoto.setImageResource(R.drawable.no_image)
            binding.imvPlus.visibility = VISIBLE
        }
    }
}