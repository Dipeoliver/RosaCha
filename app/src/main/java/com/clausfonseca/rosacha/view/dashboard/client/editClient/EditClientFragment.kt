package com.clausfonseca.rosacha.view.dashboard.client.editClient

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
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
import com.clausfonseca.rosacha.databinding.FragmentClientEditBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetRequestPermissionBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetTakePictureBinding
import com.clausfonseca.rosacha.model.ClientModel
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.extencionFunctions.checkEmptyField
import com.clausfonseca.rosacha.utils.extencionFunctions.cleanErrorValidation
import com.clausfonseca.rosacha.utils.extencionFunctions.getDbClient
import com.clausfonseca.rosacha.utils.mask.DateMask
import com.clausfonseca.rosacha.utils.mask.PhoneMask
import com.clausfonseca.rosacha.utils.mask.PhoneNumberFormatType
import com.clausfonseca.rosacha.utils.mask.validateEmailRegex
import com.clausfonseca.rosacha.view.common.CommonModelState
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class EditClientFragment : Fragment() {

    private val viewModel: EditClientViewModel by viewModels()
    private lateinit var binding: FragmentClientEditBinding
    private var selectedClientModel: ClientModel? = null
    private var pictureName: String? = ""
    private var uriImage: Uri? = null
    private var bottomSheetDialogCamera: BottomSheetDialog? = null
    private var bottomSheetDialogPermission: BottomSheetDialog? = null
    private val dialogProgress = DialogProgress()
    private var clientId: String? = null
    private var oldId: String? = null
    private var oldUrl: String = ""

    companion object {
        const val REQUEST_PERMISSION_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClientEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedClientModel = EditClientFragmentArgs.fromBundle(requireArguments()).selectedClient
        // outro metodo de recuperar dados de outro fragment
        // selectedClientModel = requireArguments().getParcelable<ClientModel>("client")
        recoverClient()
        initListeners()
        configureComponents()
        configureObservables()
        onBackPressed()
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

            is CommonModelState.CommonState.Success -> {
                dialogProgress.dismiss()
                Util.exibirToast(requireContext(), getString(R.string.update_data))
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/client_fragment")
                findNavController().navigate(uri)
            }

            is CommonModelState.CommonState.RemoveImageSuccess -> {
                // removed old image in Storage
            }

            else -> {
            }
        }
    }

    private fun initListeners() {
        binding.btnUpdateClient.setOnClickListener {
            submitForm()
        }

        binding.imvPhotoClientEdit.setOnClickListener {
            if (!binding.edtPhoneClientEdit.text.isNullOrEmpty()) showBottomSheetDialog()
            else Util.exibirToast(requireContext(), getString(R.string.required_phone_client))
        }

        binding.btnBack.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/client_fragment")
            findNavController().navigate(uri)
        }
    }

    private fun configureComponents() {
        //Mask to Phone
        val country = PhoneNumberFormatType.PT_BR // OR PhoneNumberFormatType.PT_BR
        val phoneFormatter = PhoneMask(WeakReference(binding.edtPhoneClientEdit), country)
        binding.edtPhoneClientEdit.addTextChangedListener(phoneFormatter)
//        binding.edtPhoneClient.addTextChangedListener(DateMask.mask(binding.edtPhoneClient, DateMask.FORMAT_FONE))

        //Mask to Date
        binding.edtBirthdayClientEdit.addTextChangedListener(
            DateMask.mask(
                binding.edtBirthdayClientEdit,
                DateMask.FORMAT_DATE
            )
        )
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/client_fragment")
                findNavController().navigate(uri)
            }
        })
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                uriImage?.let { uri ->
//                binding.imvPlus.visibility = View.GONE
                    binding.imvPhotoClientEdit.setImageURI(uri)
                    bottomSheetDialogCamera?.dismiss()
                }
            }
        }

    //  Capturar imagem da Camera
    private fun cameraLauncher() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        val resolver = activity?.contentResolver
        uriImage = resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        if (uriImage == null) {
            val authorization = "com.clausfonseca.rosacha"
            val directory = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageName = "$directory/${getDbClient(requireContext())}${System.currentTimeMillis()}.jpg"
            val file = File(imageName)
            uriImage = activity?.let { FileProvider.getUriForFile(it.baseContext, authorization, file) }
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage)
        cameraLauncher.launch(intent)
    }

    // apos obter imagem da galeria salva a uri e carrega o RoundedImageView
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                bottomSheetDialogCamera?.dismiss()
                binding.imvPhotoClientEdit.setImageURI(uri)
                uriImage = uri
            }
        }


    // obter imagem galeria
    private fun galleryLauncher() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        getContent.launch(intent)
    }

    // para corrigir problema de falta de imagem selecionada
    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "Image name")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //this one
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            val outputStream = context.contentResolver.openOutputStream(it)
            outputStream?.let { os ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.close()
            }
        }
        return uri
    }

    // endregion

    // region - FirebaseStorage
    // com recurso para diminuir a imagem
    private fun uploadImagem() {
        pictureName = binding.edtPhoneClientEdit.text.toString()
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
                            getDbClient(requireContext()),
                            pictureName ?: "",
                            bitmap ?: Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                        )
                        return false
                    }
                }).submit()
        }
    }
    // region - FirebaseFirestore
    // FIRESTORE--------------------------------------------------------------------------

    private fun validateData(url: String) {

        val phone = binding.edtPhoneClientEdit.text.toString()
        val name = binding.edtNameClientEdit.text.toString().trim()
        val birthday = binding.edtBirthdayClientEdit.text.toString().trim()
        val email = binding.edtEmailClientEdit.text.toString().trim().lowercase()

        if (name.isNotEmpty() && phone.length > 13) {

            if (email != "" && !email.validateEmailRegex(email)) {
                Util.exibirToast(requireContext(), getString(R.string.invalid_email_register_fragment))

            } else {
                selectedClientModel = ClientModel()
                val date = Calendar.getInstance().time
                val dateTimeFormat = SimpleDateFormat(getString(R.string.type_date), Locale.getDefault())
                val clientDate = dateTimeFormat.format(date)

                selectedClientModel?.name = name.uppercase()
                selectedClientModel?.phone = phone
                selectedClientModel?.email = email
                selectedClientModel?.birthday = birthday
                selectedClientModel?.clientDate = clientDate
                selectedClientModel?.urlImagem = url
                selectedClientModel?.id = clientId

//                when {
//                    oldId != phone -> {
//                        viewModel.removeImage(getDbClient(requireContext()), oldId.toString())
//                        //                    removeImage(oldId.toString())
//                    }
//                }

                viewModel.updateClient(getDbClient(requireContext()), selectedClientModel ?: ClientModel())
            }
        } else {
            Util.exibirToast(requireContext(), getString(R.string.required_fields))
        }

    }

    private fun recoverClient() {
        // ao carregar pagina trago os dados dos clientes
        binding.edtNameClientEdit.setText(selectedClientModel?.name.toString())
        binding.edtPhoneClientEdit.setText(selectedClientModel?.phone.toString())
        binding.edtBirthdayClientEdit.setText(selectedClientModel?.birthday.toString())
        binding.edtEmailClientEdit.setText(selectedClientModel?.email.toString())
        clientId = selectedClientModel?.id

        oldId = selectedClientModel?.phone.toString()

        val url = selectedClientModel?.urlImagem
        oldUrl = selectedClientModel?.urlImagem.toString()

        if (url == "" || url == null) Glide.with(requireContext()).load(R.drawable.no_image)
            .into(binding.imvPhotoClientEdit)
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

            }).into(binding.imvPhotoClientEdit)
        }
    }
// endregion

    // region - RequestCameraAccess
    private fun checkPermissions() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) -> {
                cameraLauncher()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                cameraLauncher()
            } else {
                bottomSheetDialogCamera?.dismiss()
                showBottomSheetDialogPermission()
            }
        }


//endregion

    // region - FieldValidation


    private fun submitForm() { // virar extension mesmo do ADDClient
        val name = checkEmptyField(binding.edtNameClientEdit, binding.nameContainer, requireContext())
        cleanErrorValidation(binding.edtNameClientEdit, binding.nameContainer)

        val phone = checkEmptyField(binding.edtPhoneClientEdit, binding.phoneContainer, requireContext(), "phone")
        cleanErrorValidation(binding.edtPhoneClientEdit, binding.phoneContainer)

        var email: Boolean = true

        if (!binding.edtEmailClientEdit.text.isNullOrEmpty()) {
            email = checkEmptyField(binding.edtEmailClientEdit, binding.emailContainer, requireContext(), "email")
        }
        cleanErrorValidation(binding.edtEmailClientEdit, binding.emailContainer)

        if (name && phone && email) {
            if (uriImage == null && binding.imvPhotoClientEdit.background != null) {
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

    // region - BottomSheetDialog
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

    private fun showBottomSheetDialog() {
        bottomSheetDialogCamera = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)

        val sheetBinding: ItemCustomBottonSheetTakePictureBinding =
            ItemCustomBottonSheetTakePictureBinding.inflate(layoutInflater, null, false)

        sheetBinding.clTakePhoto.setOnClickListener {
            checkPermissions()
        }

        sheetBinding.clGallery.setOnClickListener {
            galleryLauncher()
        }
        bottomSheetDialogCamera?.setContentView(sheetBinding.root)
        bottomSheetDialogCamera?.show()
    }
// endregion
}