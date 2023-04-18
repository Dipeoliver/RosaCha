package com.clausfonseca.rosacha.view.dashboard.product

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentProductAddBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetRequestPermissionBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetTakePictureBinding
import com.clausfonseca.rosacha.model.Product
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.view.dashboard.client.AddClientFragment
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
    private lateinit var auth: FirebaseAuth
    private lateinit var product: Product
    private var dbProducts: String = ""
    private val db = FirebaseFirestore.getInstance()
    private var pictureName: String? = ""
    private var statusOwner: Int = 0
    private var owner: String = ""

    val dialogProgress = DialogProgress()
    var bottomSheetDialogCamera: BottomSheetDialog? = null
    var bottomSheetDialogPermission: BottomSheetDialog? = null

    var uriImagem: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseStorage = Firebase.storage
        auth = Firebase.auth
        dbProducts = getString(R.string.db_product).toString()
        configureButton()
        initListeners()

        // ao clicar botão voltar abaixo
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val uri = Uri.parse("android-app://com.clausfonseca.rosacha/product_fragment")
                    findNavController().navigate(uri)
                }
            })
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
                    uriImagem = data.data


                    binding.imvPhoto.setImageURI(uriImagem)

                } else if (requestCode == 22 && uriImagem != null) {// camera

                    binding.imvPhoto.setImageURI(uriImagem)
                } else {

                }

                bottomSheetDialogCamera?.dismiss()
            }
        } else {
            // BARCODE
            var result: IntentResult? =
                IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

            if (result != null) {
                if (result.contents != null) {
                    binding.edtBarcode.setText(result.contents)
                    binding.edtReferenceProduct.requestFocus()

                } else {
                    binding.edtBarcode.setText(getString(R.string.scan_failed))
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
                binding.edtBarcode.requestFocus()
            }
        }
    }

    // BARCODE
    @Suppress("DEPRECATION")
    private fun configureButton() {
        binding.btnScan.setOnClickListener {
            checkPermissions()
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
            uriImagem =
                resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        } else { // versão antiga
            val authority = "com.clausfonseca.rosacha"
            val directory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val path = directory.path ?: ""
            val imageName = "$path/Products$pictureName.jpg"
            if (imageName == "/Products.jpg") {
                val imageName = directory.path + "/Products" + System.currentTimeMillis() + ".jpg"
            }
            val file = File(imageName)
            uriImagem =
                activity?.let { FileProvider.getUriForFile(it.baseContext, authority, file) }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImagem)
        startActivityForResult(intent, 22)
    }

    // selecionar imagem da galeria
    private fun obterImagemdaGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 11)
    }

    // com recurso para diminuir a imagem
    fun uploadImagem() {
        dialogProgress.show(childFragmentManager, "0")
        pictureName = binding.edtBarcode.text.toString()

        val reference = db.collection(dbProducts).document(pictureName.toString())
        reference.get().addOnSuccessListener { item ->
            if (item.exists()) {
                Util.exibirToast(requireContext(), getString(R.string.error_already_registered_product))
                dialogProgress.dismiss()
                binding.edtBarcode.requestFocus()
            } else {
                activity?.let {
                    Glide.with(it.baseContext).asBitmap().load(uriImagem)
                        .error(R.drawable.baseline_image_not_supported_24)
                        .apply(RequestOptions.overrideOf(800, 480))
                        .listener(object : RequestListener<Bitmap> {


                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Bitmap>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Util.exibirToast(requireContext(), getString(R.string.error_reduced_image))
                                dialogProgress.dismiss()
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
                                val reference =
                                    firebaseStorage.reference
                                        .child(dbProducts)
                                        .child("$pictureName.jpg")
                                val uploadTask = reference.putBytes(data)
                                uploadTask.continueWithTask { task ->
                                    if (!task.isSuccessful) {
                                        task.exception.let {
                                            throw it!!
                                        }
                                    }
                                    reference.downloadUrl
                                }.addOnSuccessListener { task ->
                                    var url = task.toString()
                                    validateData(url)
                                }.addOnFailureListener { error ->
                                    Util.exibirToast(
                                        requireContext(),
                                        getString(R.string.error_upload_image) + ":" + error.message.toString()
                                    )
                                    dialogProgress.dismiss()
                                }
                                return false
                            }
                        }).submit()
                }
            }
        }
    }
    // ----------------------------------------------------------------------------------

    // FIRESTORE--------------------------------------------------------------------------
    private fun validateData(url: String) {

        val barcode = binding.edtBarcode.text.toString().trim()
        val referenceProduct = binding.edtReferenceProduct.text.toString().trim()
        val description = binding.edtDescriptionProduct.text.toString().trim()
        val brand = binding.edtBrandProduct.text.toString().trim()
        val provider = binding.edtProviderProduct.text.toString().trim()
        val size = binding.edtSizeProduct.text.toString().trim()
        val color = binding.edtColorProduct.text.toString().trim()
        val costPrice = binding.edtCostProduct.text.toString().trim()
        val salesPrice = binding.edtSalesProduct.text.toString().trim()

        product = Product()

        val date = Calendar.getInstance().time
        val dateTimeFormat = SimpleDateFormat(getString(R.string.type_date), Locale.getDefault())
        val productDate = dateTimeFormat.format(date)

        product.barcode = barcode
        product.reference = referenceProduct
        product.description = description.uppercase()
        product.brand = brand.uppercase()
        product.provider = provider.uppercase()
        product.size = size
        product.color = color.uppercase()
        product.costPrice = costPrice.toDouble()
        product.salesPrice = salesPrice.toDouble()
        product.productDate = productDate
        product.urlImagem = url
        owner = if (statusOwner == 0) {
            getString(R.string.claudia)
        } else {
            getString(R.string.claudenice)
        }
        product.owner = owner
        insertProduct()
    }

    // Inserir produto no Firestore
    private fun insertProduct() {
        db.collection(dbProducts).document(product.barcode.toString())
            .set(product).addOnCompleteListener {
                Util.exibirToast(requireContext(), getString(R.string.add_success_product))
                cleaner()
                dialogProgress.dismiss()
            }.addOnFailureListener {
                Util.exibirToast(requireContext(), getString(R.string.error_save_product))
                dialogProgress.dismiss()
            }
    }

    // ----------------------------------------------------------------------------------


    // Perdir Permissão para acessar a camera -------------------------------------------------------------------------
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                AddClientFragment.REQUEST_PERMISSION_CODE
            )
            return
        }
        // verifica se quem foi pressionado foi o botão scanner
        if (binding.btnScan.isPressed) {
            val integrator: IntentIntegrator =
                IntentIntegrator.forSupportFragment(this@AddProductFragment)
            integrator.setPrompt(getString(R.string.scan_active))
            integrator.initiateScan()
        } else {
            obterImagemdaCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AddClientFragment.REQUEST_PERMISSION_CODE) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    when (grantResults[1]) {
                        PackageManager.PERMISSION_GRANTED -> {
                            checkPermissions()
                        }
                        PackageManager.PERMISSION_DENIED -> {
                            bottomSheetDialogCamera?.dismiss()
                            showBottomSheetDialogPermission()
                        }
                    }
                }

                PackageManager.PERMISSION_DENIED -> {
                    if (!shouldShowRequestPermissionRationale(permissions[0])) {
                        bottomSheetDialogCamera?.dismiss()
                        showBottomSheetDialogPermission()
                    }
                }
            }
        }
    }

    private fun showBottomSheetDialogPermission() {
        bottomSheetDialogPermission = BottomSheetDialog(requireContext(),R.style.BottomSheetDialogTheme)
        val sheetBinding: ItemCustomBottonSheetRequestPermissionBinding =
            ItemCustomBottonSheetRequestPermissionBinding.inflate(layoutInflater, null, false)

        sheetBinding.btnCancel.setOnClickListener {
            bottomSheetDialogPermission?.dismiss()
        }

        sheetBinding.btnConfig.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", requireActivity().packageName, null)
            intent.data = uri
            requireContext().startActivity(intent)
            bottomSheetDialogPermission?.dismiss()
        }

        bottomSheetDialogPermission?.setContentView(sheetBinding.root)
        bottomSheetDialogPermission?.show()
    }

    // ----------------------------------------------------------------------------------------------------------------


    private fun initListeners() {
        binding.btnAddProduct.setOnClickListener {
            // verificar sinal de internet (FAZER)

            if (binding.edtBarcode.text.isNotEmpty() &&
                binding.edtDescriptionProduct.text.isNotEmpty() &&
                binding.edtSizeProduct.text.isNotEmpty() &&
                binding.edtCostProduct.text.isNotEmpty() &&
                binding.edtSalesProduct.text.isNotEmpty()
            ) {
                if (uriImagem != null) {
                    uploadImagem()
                } else {

                    // SE NÃO TIVER IMAGEM  O URI E PREENCHIDO COM IMAGEM PADRÃO
                    val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.no_image)
                    val bitmap = drawable?.toBitmap()
                    uriImagem = getImageUriFromBitmap(requireContext(), bitmap!!)
                    uploadImagem()
                }
            } else {
                Util.exibirToast(requireContext(), getString(R.string.required_fields))
            }
        }
        binding.rgOwnerProduct.setOnCheckedChangeListener { _, id ->
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
            if (binding.edtBarcode.text.isNotEmpty()) showBottomSheetDialog()
            else Util.exibirToast(requireContext(), getString(R.string.required_barcode_product))
        }
    }


    // para corrigir problema de falta de imagem selecionada
    fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }


    private fun showBottomSheetDialog() {
        bottomSheetDialogCamera = BottomSheetDialog(requireContext(),R.style.BottomSheetDialogTheme)

        val sheetBinding: ItemCustomBottonSheetTakePictureBinding =
            ItemCustomBottonSheetTakePictureBinding.inflate(layoutInflater, null, false)

        sheetBinding.imvBottomPhoto.setOnClickListener {
            checkPermissions()
        }
        sheetBinding.txtBottomPhoto.setOnClickListener {
            checkPermissions()
        }

        sheetBinding.imvBottomGallery.setOnClickListener {
            obterImagemdaGaleria()
        }
        sheetBinding.txtBottomGallery.setOnClickListener {
            obterImagemdaGaleria()
        }
        bottomSheetDialogCamera?.setContentView(sheetBinding.root)
        bottomSheetDialogCamera?.show()
    }

    private fun cleaner() {
        binding.apply {
            edtBarcode.text.clear()
            edtReferenceProduct.text.clear()
            edtDescriptionProduct.text.clear()
            edtBrandProduct.text.clear()
            edtProviderProduct.text.clear()
            edtSizeProduct.text.clear()
            edtColorProduct.text.clear()
            edtCostProduct.text.clear()
            edtSalesProduct.text.clear()
            edtBarcode.requestFocus()
            binding.imvPhoto.setImageResource(R.drawable.no_image)
            binding.imvPlus.visibility = VISIBLE
        }
    }
}