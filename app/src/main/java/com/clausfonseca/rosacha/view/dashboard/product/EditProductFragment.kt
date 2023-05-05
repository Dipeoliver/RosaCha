package com.clausfonseca.rosacha.view.dashboard.product

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.Context.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.clausfonseca.rosacha.databinding.FragmentProductEditBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetRequestPermissionBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetTakePictureBinding
import com.clausfonseca.rosacha.model.Product
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.extencionFunctions.checkEmptyField
import com.clausfonseca.rosacha.utils.extencionFunctions.cleanErrorValidation
import com.clausfonseca.rosacha.view.dashboard.client.AddClientFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EditProductFragment : Fragment() {
    private lateinit var binding: FragmentProductEditBinding
    private lateinit var firebaseStorage: FirebaseStorage
    private var selectedProduct: Product? = null
    private var pictureName: String? = ""
    private var dbProducts: String = ""
    private var uriImage: Uri? = null
    var bottomSheetDialogCamera: BottomSheetDialog? = null
    var bottomSheetDialogPermission: BottomSheetDialog? = null
    private val db = FirebaseFirestore.getInstance()
    private var statusOwner: Int = 0
    private var owner: String = ""
    var productId: String? = null
    var url: String? = null
    var oldUrl: String = ""
    private var insertStatus: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedProduct = EditProductFragmentArgs.fromBundle(requireArguments()).selectedProduct
        firebaseStorage = Firebase.storage
        dbProducts = getString(R.string.db_product).toString()
        recoverProduct()
        onBackPressed()
        initListeners()
    }

    private fun initListeners() {
        binding.btnUpdateProduct.setOnClickListener {
            submitForm()
        }

        binding.imvPhoto.setOnClickListener {
            showBottomSheetDialog()
        }

        binding.btnBackEdit.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/product_fragment")
            findNavController().navigate(uri)
        }

        binding.rgOwnerProduct.setOnCheckedChangeListener { _, id ->
            statusOwner = when (id) {
                R.id.claudia -> 0
                else -> 1
            }
        }

        binding.btnCopy.setOnClickListener {
            if (insertStatus == true) {
                binding.edtBarcode.isEnabled = false
                binding.btnCopy.setImageDrawable(resources.getDrawable(R.drawable.baseline_content_copy_24))
                binding.btnUpdateProduct.text = "UPDATE"
                binding.txtProductTitle.text = "Update Product"
                insertStatus = false
            } else {
                binding.edtBarcode.isEnabled = true
                binding.btnCopy.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_cancel_24))
                binding.btnUpdateProduct.text = "INSERT"
                binding.txtProductTitle.text = "Insert Product"
                insertStatus = true
            }
            copyBarcode()
        }

        binding.costContainer.helperText = ""
        binding.salesContainer.helperText = ""
        binding.sizeContainer.helperText = ""
        binding.descriptionContainer.helperText = ""
    }

    // region - RequestCameraAccess
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
        obterImagemdaCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
        bottomSheetDialogPermission = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
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

// endregion

    // region - ImageView
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // setar a imagem na ImageView

        if (requestCode == 11 || requestCode == 22) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
                binding.imvPlus.visibility = View.GONE

                if (requestCode == 11 && data != null) {  // galeria
                    uriImage = data.data

                    binding.imvPhoto.setImageURI(uriImage)

                } else if (requestCode == 22 && uriImage != null) {// camera

                    binding.imvPhoto.setImageURI(uriImage)
                }
                bottomSheetDialogCamera?.dismiss()
            }
        }
    }
    // endregion

    // region - FieldValidation
    private fun submitForm() {
        val validBarcode = checkEmptyField(binding.edtBarcode, binding.barcodeContainer, requireContext())
        cleanErrorValidation(binding.edtBarcode, binding.barcodeContainer)

        val validDescription = checkEmptyField(binding.edtDescriptionProduct, binding.descriptionContainer, requireContext())
        cleanErrorValidation(binding.edtDescriptionProduct, binding.descriptionContainer)

        val validSize = checkEmptyField(binding.edtSizeProduct, binding.sizeContainer, requireContext())
        cleanErrorValidation(binding.edtSizeProduct, binding.sizeContainer)

        val validCost = checkEmptyField(binding.edtCostProduct, binding.costContainer, requireContext())
        cleanErrorValidation(binding.edtCostProduct, binding.costContainer)

        val validSales = checkEmptyField(binding.edtSalesProduct, binding.salesContainer, requireContext())
        cleanErrorValidation(binding.edtSalesProduct, binding.salesContainer)


        if (validBarcode && validDescription && validSize && validCost && validSales) {
            if (uriImage == null && binding.imvPhoto.getBackground() != null) {
                // SE NÃO TIVER IMAGEM  O URI E PREENCHIDO COM IMAGEM PADRÃO
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.no_image)
                val bitmap = drawable?.toBitmap()
                uriImage = getImageUriFromBitmap(requireContext(), bitmap!!)
                uploadImagem()
            } else if (uriImage != null) {
                uploadImagem()
            } else {
                validateData(oldUrl)
            }
        }
    }


    // endregion

    // region - FirebaseStorage
    //  Capturar imagem da Camera
    private fun obterImagemdaCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // versão nova
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            val resolver = activity?.contentResolver
            uriImage =
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
            uriImage = activity?.let { FileProvider.getUriForFile(it.baseContext, authority, file) }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage)
        startActivityForResult(intent, 22)
    }

    // selecionar imagem da galeria
    private fun obterImagemdaGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 11)
    }

    // com recurso para diminuir a imagem
    private fun uploadImagem() {

        pictureName = binding.edtBarcode.text.toString()
        activity?.let {
            Glide.with(it.baseContext).asBitmap().load(uriImage).error(R.drawable.no_image)
                .apply(RequestOptions.overrideOf(800, 480)).listener(object : RequestListener<Bitmap> {


                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Util.exibirToast(requireContext(), getString(R.string.error_reduced_image))
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
                                task.exception.let { it ->
                                    throw it!!
                                }
                            }
                            reference.downloadUrl
                        }.addOnSuccessListener { task ->
                            val url = task.toString()
                            validateData(url)
                        }.addOnFailureListener { error ->
                            Util.exibirToast(
                                requireContext(),
                                getString(R.string.error_upload_image) + ":" + error.message.toString()
                            )
                        }
                        return false
                    }
                }).submit()
        }
    }

    //remover imagem antiga da galeria
    private fun removeImage(id: String) {
        val reference = firebaseStorage.reference.child(dbProducts).child("${id}.jpg")
        reference.delete().addOnSuccessListener { task ->
        }.addOnFailureListener { error ->
            Util.exibirToast(requireContext(), getString(R.string.error_delete_image) + error.message.toString())
        }
    }

    // para corrigir problema de falta de imagem selecionada
    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }
    // endregion

    // region - FirebaseFirestore
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

        if (barcode.isNotEmpty() && description.isNotEmpty() && size.isNotEmpty() && costPrice.isNotEmpty() && salesPrice.isNotEmpty()
        ) {
//            dialogProgress.show(childFragmentManager, "0")
            selectedProduct = Product()

            val date = Calendar.getInstance().time
            val dateTimeFormat = SimpleDateFormat(getString(R.string.type_date), Locale.getDefault())
            val productDate = dateTimeFormat.format(date)

            selectedProduct?.barcode = barcode
            selectedProduct?.reference = referenceProduct
            selectedProduct?.description = description.uppercase()
            selectedProduct?.brand = brand.uppercase()
            selectedProduct?.provider = provider.uppercase()
            selectedProduct?.size = size
            selectedProduct?.color = color.uppercase()
            selectedProduct?.costPrice = costPrice.toDouble()
            selectedProduct?.salesPrice = salesPrice.toDouble()
            selectedProduct?.productDate = productDate
            selectedProduct?.urlImagem = url
            selectedProduct?.id = productId
            owner = if (statusOwner == 0) {
                getString(R.string.claudia)
            } else {
                getString(R.string.claudenice)
            }
            selectedProduct?.owner = owner


            // Verificação para ver se esta inserindo ou atualizando o produto
            if (insertStatus == false) {
                updateProduct(selectedProduct!!)
            } else {
                insertProduct(selectedProduct!!)

            }
        }
    }

    private fun insertProduct(product: Product) {
        db!!.collection(dbProducts).document(product.barcode.toString())
            .set(product).addOnCompleteListener {
                Util.exibirToast(requireContext(), getString(R.string.add_success_product))
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/product_fragment")
                findNavController().navigate(uri)
            }.addOnFailureListener {
                Util.exibirToast(requireContext(), getString(R.string.error_save_product))
            }
    }

    private fun updateProduct(selectedProduct: Product) {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        if (selectedProduct != null) {

            val reference = db!!.collection(dbProducts)

            val client = hashMapOf(
                // posso fazer update de apenas 1 campo se necessário
                "reference" to selectedProduct.reference,
                "description" to selectedProduct.description,
                "brand" to selectedProduct.brand,
                "provider" to selectedProduct.provider,
                "size" to selectedProduct.size,
                "color" to selectedProduct.color,
                "costPrice" to selectedProduct.costPrice,
                "salesPrice" to selectedProduct.salesPrice,
                "productDate" to selectedProduct.productDate,
                "urlImagem" to selectedProduct.urlImagem,
                "owner" to selectedProduct.owner,
                "id" to selectedProduct.id
            )
            reference.document(selectedProduct.barcode.toString()).update(client as Map<String, Any>).addOnSuccessListener {
                Util.exibirToast(requireContext(), getString(R.string.update_data))
                dialogProgress.dismiss()
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/product_fragment")
                findNavController().navigate(uri)
            }.addOnFailureListener { error ->
                dialogProgress.dismiss()
                Util.exibirToast(requireContext(), getString(R.string.error_update_data_product) + ":" + error.message.toString())
            }
        }
    }

    private fun recoverProduct() {
        binding.edtBarcode.setText(selectedProduct?.barcode.toString())
        binding.edtReferenceProduct.setText(selectedProduct?.reference.toString())
        binding.edtDescriptionProduct.setText(selectedProduct?.description.toString())
        binding.edtBrandProduct.setText(selectedProduct?.brand.toString())
        binding.edtProviderProduct.setText(selectedProduct?.provider.toString())
        binding.edtSizeProduct.setText(selectedProduct?.size.toString())
        binding.edtColorProduct.setText(selectedProduct?.color.toString())
        binding.edtCostProduct.setText(selectedProduct?.costPrice.toString())
        binding.edtSalesProduct.setText(selectedProduct?.salesPrice.toString())

        productId = selectedProduct?.id
        url = selectedProduct?.urlImagem
        oldUrl = selectedProduct?.urlImagem.toString()


        if (selectedProduct?.owner == getString(R.string.claudia)) {
            binding.claudia.isChecked = true
        } else {
            binding.claudenice.isChecked = true
        }

        if (url == "" || url == null) Glide.with(requireContext()).load(R.drawable.no_image)
            .into(binding.imvPhoto)
        else {
            Glide.with(requireContext()).asBitmap().load(url).listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

            }).into(binding.imvPhoto)
        }
    }

    // endregion

    private fun copyBarcode() {
        val textToCopy = binding.edtBarcode.text
        val myClipboard: ClipboardManager = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val myClip: ClipData
        val clipData = ClipData.newPlainText("text", textToCopy)
        myClipboard.setPrimaryClip(clipData)
        Util.exibirToast(requireContext(), getString(R.string.copy_barcode_product))
//        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
    }

    private fun showBottomSheetDialog() {
        bottomSheetDialogCamera = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)

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

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val uri = Uri.parse("android-app://com.clausfonseca.rosacha/product_fragment")
                    findNavController().navigate(uri)
                }
            })
    }


}