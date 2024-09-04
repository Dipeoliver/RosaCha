package com.clausfonseca.rosacha.view.dashboard.product.editProduct

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.clausfonseca.rosacha.model.ProductModel
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.extencionFunctions.checkEmptyField
import com.clausfonseca.rosacha.utils.extencionFunctions.cleanErrorValidation
import com.clausfonseca.rosacha.utils.extencionFunctions.getDbProduct
import com.clausfonseca.rosacha.view.common.CommonModelState
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class EditProductFragment : Fragment() {
    private val viewModel: EditProductViewModel by viewModels()
    private lateinit var binding: FragmentProductEditBinding
    private var selectedProductModel: ProductModel? = null
    private var pictureName: String? = ""
    private val dialogProgress = DialogProgress()
    private var uriImage: Uri? = null
    private var bottomSheetDialogCamera: BottomSheetDialog? = null
    private var bottomSheetDialogPermission: BottomSheetDialog? = null
    private var statusOwner: Int = 0
    private var owner: String = ""
    private var productId: String? = null
    private var url: String? = null
    private var oldUrl: String = ""
    private var insertStatus: Boolean = false
    private var quantity = 0
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val barcodeEditText = binding.edtBarcode
    private val photoImageView = binding.imvPhoto

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedProductModel = EditProductFragmentArgs.fromBundle(requireArguments()).selectedProductModel
        recoverProduct()
        onBackPressed()
        initListeners()
        configureObservables()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializar o ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri: Uri? = data?.data
                // Faça algo com o imageUri, como carregar a imagem em uma ImageView
            }
        }
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                uriImage?.let { uri ->
                    // Faça algo com o uri, como carregar a imagem em uma ImageView
                }
            }
        }
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                uriImage = data?.data
                binding.imvPlus.visibility = View.GONE
                photoImageView.setImageURI(uriImage)
                bottomSheetDialogCamera?.dismiss()
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            val storageGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false

            if (cameraGranted && storageGranted) {
                captureImageFromCamera()
            } else {
                // Se alguma permissão for negada
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                    !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ) {
                    bottomSheetDialogCamera?.dismiss()
                    showBottomSheetDialogPermission()
                }
            }
        }
    }

    private fun configureObservables() {
        viewModel.model.screenState.observe(viewLifecycleOwner) {
            handleState(it)
        }
    }

    private fun handleState(state: CommonModelState.CommonState?) {
        when (state) {
            is CommonModelState.CommonState.Loading -> {
                if (state.isLoading) dialogProgress.show(childFragmentManager, "0")
                else dialogProgress.dismiss()
            }

            is CommonModelState.CommonState.Error -> {
                Util.exibirToast(requireContext(), getString(R.string.error_update_data_client) + ":" + state.message)
            }

            is CommonModelState.CommonState.SuccessStorageUrl -> {
                validateData(state.data)
            }

            is CommonModelState.CommonState.SuccessUpdate -> {
                dialogProgress.dismiss()
                Util.exibirToast(requireContext(), getString(R.string.update_product))
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/product_fragment")
                findNavController().navigate(uri)
            }

            is CommonModelState.CommonState.InsertProductSuccess -> {
                dialogProgress.dismiss()
                Util.exibirToast(requireContext(), getString(R.string.add_product))
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/product_fragment")
                findNavController().navigate(uri)
            }

            else -> {
            }
        }
    }

    private fun initListeners() {
        binding.imvAdd.setOnClickListener {
            quantity += 1
            updateQuantity()
        }

        binding.imvSub.setOnClickListener {
            if (quantity >= 1) quantity -= 1
            updateQuantity()
        }

        binding.btnUpdateProduct.setOnClickListener {
            submitForm()
        }

        photoImageView.setOnClickListener {
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
            insertStatus = !insertStatus

            barcodeEditText.isEnabled = !insertStatus
            binding.btnCopy.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (insertStatus) R.drawable.ic_baseline_cancel_24 else R.drawable.baseline_content_copy_24
                )
            )
            barcodeEditText.isEnabled = insertStatus
            binding.btnUpdateProduct.text = if (insertStatus) "INSERT" else "UPDATE"
            binding.txtProductTitle.text = if (insertStatus) "Insert ProductModel" else "Update ProductModel"

            copyBarcode()
        }


        binding.costContainer.helperText = ""
        binding.salesContainer.helperText = ""
        binding.sizeContainer.helperText = ""
        binding.descriptionContainer.helperText = ""
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Solicitar permissões
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
        } else {
            // Permissões já concedidas
            captureImageFromCamera()
        }
    }

//    @Deprecated("Deprecated in Java")
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == AddClientFragment.REQUEST_PERMISSION_CODE) {
//            when (grantResults[0]) {
//                PackageManager.PERMISSION_GRANTED -> {
//                    when (grantResults[1]) {
//                        PackageManager.PERMISSION_GRANTED -> {
//                            checkPermissions()
//                        }
//
//                        PackageManager.PERMISSION_DENIED -> {
//                            bottomSheetDialogCamera?.dismiss()
//                            showBottomSheetDialogPermission()
//                        }
//                    }
//                }
//
//                PackageManager.PERMISSION_DENIED -> {
//                    if (!shouldShowRequestPermissionRationale(permissions[0])) {
//                        bottomSheetDialogCamera?.dismiss()
//                        showBottomSheetDialogPermission()
//                    }
//                }
//            }
//        }
//    }

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


    // region - FieldValidation
    private fun submitForm() {
        val validBarcode = checkEmptyField(barcodeEditText, binding.barcodeContainer, requireContext())
        cleanErrorValidation(barcodeEditText, binding.barcodeContainer)

        val validDescription = checkEmptyField(binding.edtDescriptionProduct, binding.descriptionContainer, requireContext())
        cleanErrorValidation(binding.edtDescriptionProduct, binding.descriptionContainer)

        val validSize = checkEmptyField(binding.edtSizeProduct, binding.sizeContainer, requireContext())
        cleanErrorValidation(binding.edtSizeProduct, binding.sizeContainer)

        val validCost = checkEmptyField(binding.edtCostProduct, binding.costContainer, requireContext())
        cleanErrorValidation(binding.edtCostProduct, binding.costContainer)

        val validSales = checkEmptyField(binding.edtSalesProduct, binding.salesContainer, requireContext())
        cleanErrorValidation(binding.edtSalesProduct, binding.salesContainer)

        if (validBarcode && validDescription && validSize && validCost && validSales) {
            if (uriImage == null && photoImageView.background != null) {
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

    private fun captureImageFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        uriImage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            }
            val resolver = activity?.contentResolver
            resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            val authority = "com.clausfonseca.rosacha"
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val imageName = directory.path + "/Products" + System.currentTimeMillis() + ".jpg"
            val file = File(imageName)
            activity?.let { FileProvider.getUriForFile(it.baseContext, authority, file) }
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        cameraLauncher.launch(intent)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }


    // com recurso para diminuir a imagem
    private fun uploadImagem() {

        pictureName = barcodeEditText.text.toString()
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

                        viewModel.getUrlStorage(
                            getDbProduct(requireContext()),
                            pictureName ?: "",
                            bitmap ?: Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                        )
                        return false
                    }
                }).submit()
        }
    }

    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val contentResolver = context.contentResolver
        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let { uri ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)
        }

        return imageUri
    }
    // endregion

    // region - FirebaseFirestore
    private fun validateData(url: String) {

        val barcode = barcodeEditText.text.toString().trim()
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
            selectedProductModel = ProductModel()

            val date = Calendar.getInstance().time
            val dateTimeFormat = SimpleDateFormat(getString(R.string.type_date), Locale.getDefault())
            val productDate = dateTimeFormat.format(date)

            selectedProductModel?.barcode = barcode
            selectedProductModel?.reference = referenceProduct
            selectedProductModel?.description = description.uppercase()
            selectedProductModel?.quantity = quantity
            selectedProductModel?.brand = brand.uppercase()
            selectedProductModel?.provider = provider.uppercase()
            selectedProductModel?.size = size
            selectedProductModel?.color = color.uppercase()
            selectedProductModel?.costPrice = costPrice.toDouble()
            selectedProductModel?.salesPrice = salesPrice.toDouble()
            selectedProductModel?.productDate = productDate
            selectedProductModel?.urlImagem = url
            selectedProductModel?.id = productId
            owner = if (statusOwner == 0) {
                getString(R.string.claudia)
            } else {
                getString(R.string.claudenice)
            }
            selectedProductModel?.owner = owner


            // Verificação para ver se esta inserindo ou atualizando o produto
            if (!insertStatus) {
                viewModel.updateProduct(getDbProduct(requireContext()), selectedProductModel!!)
//                updateProduct(selectedProductModel!!)
            } else {
                viewModel.insertProduct(getDbProduct(requireContext()), selectedProductModel!!)
//                insertProduct(selectedProductModel!!)
            }
        }
    }

    private fun recoverProduct() {
        barcodeEditText.setText(selectedProductModel?.barcode.toString())
        binding.edtReferenceProduct.setText(selectedProductModel?.reference.toString())
        binding.edtDescriptionProduct.setText(selectedProductModel?.description.toString())
        binding.edtBrandProduct.setText(selectedProductModel?.brand.toString())
        binding.edtProviderProduct.setText(selectedProductModel?.provider.toString())
        binding.edtSizeProduct.setText(selectedProductModel?.size.toString())
        binding.edtColorProduct.setText(selectedProductModel?.color.toString())
        binding.edtCostProduct.setText(selectedProductModel?.costPrice.toString())
        binding.edtSalesProduct.setText(selectedProductModel?.salesPrice.toString())
        binding.edtQuantityProduct.setText(selectedProductModel?.quantity.toString())
        quantity = selectedProductModel?.quantity ?: 0

        productId = selectedProductModel?.id
        url = selectedProductModel?.urlImagem
        oldUrl = selectedProductModel?.urlImagem.toString()


        if (selectedProductModel?.owner == getString(R.string.claudia)) {
            binding.claudia.isChecked = true
        } else {
            binding.claudenice.isChecked = true
        }

        if (url == "" || url == null) Glide.with(requireContext()).load(R.drawable.no_image)
            .into(photoImageView)
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

            }).into(photoImageView)
        }
    }

    // endregion

    private fun copyBarcode() {
        val textToCopy = barcodeEditText.text
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

        sheetBinding.clTakePhoto.setOnClickListener {
            checkPermissions()
        }

        sheetBinding.clGallery.setOnClickListener {
            pickImageFromGallery()
        }
        bottomSheetDialogCamera?.setContentView(sheetBinding.root)
        bottomSheetDialogCamera?.show()
    }

    private fun updateQuantity() {
        binding.edtQuantityProduct.setText(quantity.toString())
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