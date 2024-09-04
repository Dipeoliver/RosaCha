package com.clausfonseca.rosacha.view.dashboard.product.addProduct

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
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
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.databinding.FragmentProductAddBinding
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
@Suppress("DEPRECATION")
class AddProductFragment : Fragment() {
    private val viewModel: AddProductViewModel by viewModels()
    private lateinit var binding: FragmentProductAddBinding
    private var uriImage: Uri? = null
    private lateinit var productModel: ProductModel
    private var pictureName: String? = ""
    private var statusOwner: Int = 0
    private var owner: String = ""
    private val dialogProgress = DialogProgress()
    private var bottomSheetDialogCamera: BottomSheetDialog? = null
    private var bottomSheetDialogPermission: BottomSheetDialog? = null
    private var quantity = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        updateQuantity()
        configureObservables()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val uri = Uri.parse("android-app://com.clausfonseca.rosacha/product_fragment")
                    findNavController().navigate(uri)
                }
            })
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

            is CommonModelState.CommonState.SuccessStorageUrl -> {
                validateData(state.data)
            }

            is CommonModelState.CommonState.Success -> {

                if (viewModel.model.dataUrl) {
                    Util.exibirToast(requireContext(), getString(R.string.error_already_registered_product))
                    dialogProgress.dismiss()
                    binding.edtBarcode.requestFocus()
                } else {
                    activity?.let {
                        Glide.with(it.baseContext).asBitmap().load(uriImage)
                            .error(R.drawable.no_image)
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
            }

            is CommonModelState.CommonState.Error -> {
                Util.exibirToast(requireContext(), getString(FirebaseHelper.validError(state.message)))
            }


            is CommonModelState.CommonState.InsertProductSuccess -> {
                if (state.data) {
                    Util.exibirToast(requireContext(), getString(R.string.add_success_product))
                    cleaner()
                } else {
                    Util.exibirToast(requireContext(), getString(R.string.error_save_product))
                }
            }

            else -> {
            }
        }
    }


    private fun initListeners() {
        binding.btnAddProduct.setOnClickListener {
            // verificar sinal de internet (FAZER)
            submitForm()
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
            if (!binding.edtBarcode.text.isNullOrEmpty()) showBottomSheetDialog()
            else Util.exibirToast(requireContext(), getString(R.string.required_barcode_product))
        }

        binding.imvAdd.setOnClickListener {
            quantity += 1
            updateQuantity()
        }

        binding.imvSub.setOnClickListener {
            if (quantity >= 1) quantity -= 1
            updateQuantity()
        }

        binding.btnScan.setOnClickListener {
            checkPermissions()
        }
    }

    // region - RequestCameraAccess
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                obterImagemdaCamera()
            } else {
                bottomSheetDialogCamera?.dismiss()
                showBottomSheetDialogPermission()
            }
        }

    private fun checkPermissions() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) -> {
                obterImagemdaCamera()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
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

    // region - Barcode&&ImageView

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // setar a imagem na ImageView

        if (requestCode == 11 || requestCode == 22) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
//                binding.imvPlus.visibility = View.GONE

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

        val validDescription =
            checkEmptyField(binding.edtDescriptionProduct, binding.descriptionContainer, requireContext())
        cleanErrorValidation(binding.edtDescriptionProduct, binding.descriptionContainer)

        val validSize = checkEmptyField(binding.edtSizeProduct, binding.sizeContainer, requireContext())
        cleanErrorValidation(binding.edtSizeProduct, binding.sizeContainer)

        val validCost = checkEmptyField(binding.edtCostProduct, binding.costContainer, requireContext())
        cleanErrorValidation(binding.edtCostProduct, binding.costContainer)

        val validSales = checkEmptyField(binding.edtSalesProduct, binding.salesContainer, requireContext())
        cleanErrorValidation(binding.edtSalesProduct, binding.salesContainer)

        if (validBarcode && validDescription && validSize && validCost && validSales) {
            if (uriImage != null) {
                getImage()
            } else {
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.no_image)
                val bitmap = drawable?.toBitmap()
                uriImage = getImageUriFromBitmap(requireContext(), bitmap!!)
                getImage()
            }
        }
    }
    // endregion

    // region - FirebaseStorage
    private fun obterImagemdaCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        val resolver = activity?.contentResolver
        uriImage = resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        if (uriImage == null) {
            val authorization = "com.clausfonseca.rosacha"
            val directory = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageName = "$directory/${getDbProduct(requireContext())}${System.currentTimeMillis()}.jpg"
            val file = File(imageName)
            uriImage = activity?.let { FileProvider.getUriForFile(it.baseContext, authorization, file) }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage)
        cameraLauncher.launch(intent)
    }

    private val cameraLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                bottomSheetDialogCamera?.dismiss()
                binding.imvPhoto.setImageURI(uriImage)
            }
        }

    // para corrigir problema de falta de imagem selecionada
    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

    private fun getImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 11)
    }
    // com recurso para diminuir a imagem
    private fun getImage() {
        pictureName = binding.edtBarcode.text.toString()
        viewModel.getUrlFile(getDbProduct(requireContext()), pictureName ?: "")
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

        productModel = ProductModel()

        val date = Calendar.getInstance().time
        val dateTimeFormat = SimpleDateFormat(getString(R.string.type_date), Locale.getDefault())
        val productDate = dateTimeFormat.format(date)

        productModel.barcode = barcode
        productModel.reference = referenceProduct
        productModel.description = description.uppercase()
        productModel.quantity = quantity
        productModel.brand = brand.uppercase()
        productModel.provider = provider.uppercase()
        productModel.size = size
        productModel.color = color.uppercase()
        productModel.costPrice = costPrice.toDouble()
        productModel.salesPrice = salesPrice.toDouble()
        productModel.productDate = productDate
        productModel.urlImagem = url
        owner = if (statusOwner == 0) {
            getString(R.string.claudia)
        } else {
            getString(R.string.claudenice)
        }
        productModel.owner = owner
        productModel.qtySales = 1
        viewModel.insertProduct(getDbProduct(requireContext()), productModel)
    }
    // endregion

    private fun showBottomSheetDialog() {
        bottomSheetDialogCamera = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)

        val sheetBinding: ItemCustomBottonSheetTakePictureBinding =
            ItemCustomBottonSheetTakePictureBinding.inflate(layoutInflater, null, false)

        sheetBinding.clTakePhoto.setOnClickListener {
            checkPermissions()
        }

        sheetBinding.clGallery.setOnClickListener {
            getImageFromGallery()
        }
        bottomSheetDialogCamera?.setContentView(sheetBinding.root)
        bottomSheetDialogCamera?.show()
    }

    private fun updateQuantity() {
        binding.edtQuantityProduct.setText(quantity.toString())
    }

    private fun cleaner() {
        binding.apply {
            edtBarcode.text = null
            edtReferenceProduct.text = null
            edtDescriptionProduct.text = null
            edtBrandProduct.text = null
            edtProviderProduct.text = null
            edtSizeProduct.text = null
            edtColorProduct.text = null
            edtCostProduct.text = null
            edtSalesProduct.text = null
            binding.imvPhoto.setImageResource(R.drawable.no_image)
            binding.imvPlus.visibility = VISIBLE
            quantity = 1
            updateQuantity()
            binding.edtBarcode.requestFocus()
        }
    }
}