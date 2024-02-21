package com.clausfonseca.rosacha.view.dashboard.client.addClient

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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
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
import com.clausfonseca.rosacha.databinding.FragmentClientAddBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetRequestPermissionBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetTakePictureBinding
import com.clausfonseca.rosacha.model.ClientModel
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.extencionFunctions.checkEmptyField
import com.clausfonseca.rosacha.utils.extencionFunctions.cleanErrorValidation
import com.clausfonseca.rosacha.utils.mask.DateMask
import com.clausfonseca.rosacha.utils.mask.PhoneMask
import com.clausfonseca.rosacha.utils.mask.PhoneNumberFormatType
import com.clausfonseca.rosacha.view.onboarding.CommonModelState
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddClientFragment : Fragment() {

    private lateinit var binding: FragmentClientAddBinding

    private lateinit var clientModel: ClientModel
    private var dbClients: String = ""
    private val viewModel: AddClientViewModel by viewModels()
    private var pictureName: String? = ""
    var uriImagem: Uri? = null
    var bottomSheetDialogPermission: BottomSheetDialog? = null
    var bottomSheetDialogCamera: BottomSheetDialog? = null
    val dialogProgress = DialogProgress()
    var email: String = ""

    private val REQUEST_CAMERA_PERMISSION = 1001

    companion object {
        const val REQUEST_PERMISSION_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClientAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbClients = getString(R.string.db_client)
        initListeners()
        configureComponents()
        onBackPressed()
        configureObservables()

    }

    private fun initListeners() {
        binding.btnAddClient.setOnClickListener {
            submitForm()
        }

        binding.imvPhotoClient.setOnClickListener {
            if (!binding.edtPhoneClient.text.isNullOrEmpty()) showBottomSheetDialogCamera()
            else Util.exibirToast(requireContext(), getString(R.string.required_phone_client))
        }

        binding.btnBack.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/client_fragment")
            findNavController().navigate(uri)
        }
    }


    // region - Camera & ImageView
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // setar a imagem na ImageView
        if (requestCode == 11 || requestCode == 22) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
                binding.imvPlus.visibility = View.GONE

                if (requestCode == 11 && data != null) {  // galeria
                    uriImagem = data.data

                    binding.imvPhotoClient.setImageURI(uriImagem)

                } else if (requestCode == 22 && uriImagem != null) {// camera

                    binding.imvPhotoClient.setImageURI(uriImagem)
                }
                bottomSheetDialogCamera?.dismiss()
            }
        }
    }

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
            val authorization = "com.clausfonseca.rosacha"
            val directory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val path = directory.path ?: ""
            val imageName = "$path/$dbClients$pictureName.jpg"
            if (imageName == "/$dbClients.jpg") {
                val imageName = directory.path + "/" + dbClients + System.currentTimeMillis() + ".jpg"
            }
            val file = File(imageName)
            uriImagem =
                activity?.let { FileProvider.getUriForFile(it.baseContext, authorization, file) }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImagem)
        startActivityForResult(intent, 22)
    }

    // apos obter imagem da galeria salva a uri e carrega o RoundedImageView
    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            bottomSheetDialogCamera?.dismiss()
            binding.imvPhotoClient.setImageURI(uri)
            uriImagem = uri
        }
    }
    // obter imagem galeria
    private fun obterImagemdaGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        getContent.launch(intent)
    }


    // para corrigir problema de falta de imagem selecionada
    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }
    // endregion

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
//                progressState(state.isLoading)
            }

            is CommonModelState.CommonState.SuccessStorageUrl -> {

                validateData(state.data)

                // esta o erro generico e tem de criar um novo stage.
            }

            is CommonModelState.CommonState.Success -> {

                if (viewModel.model.dataUrl) {
                    Util.exibirToast(requireContext(), getString(R.string.error_already_registered_client))
                    dialogProgress.dismiss()
                    binding.edtPhoneClient.requestFocus()
                } else {
                    activity?.let {
                        Glide.with(it.baseContext).asBitmap().load(uriImagem).error(R.drawable.no_image)
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

                                    // REVISAR AQUI
                                    viewModel.getUrlStorage(
                                        dbClients,
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
                Toast.makeText(
                    requireContext(),
                    FirebaseHelper.validError(state.message),
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                println()
            }
        }
    }


    // region - FirebaseStorage
    // com recurso para diminuir a imagem
    fun uploadImagem() {

        pictureName = binding.edtPhoneClient.text.toString()
        viewModel.getFileUrl(pictureName ?: "")


//        val reference = viewModel.db.collection("@string/").document(pictureName.toString()+".jpg")
//        reference.get().addOnSuccessListener { item ->
//            if (item.exists()) {
//                Util.exibirToast(requireContext(), getString(R.string.error_already_registered_client))
//                dialogProgress.dismiss()
//                binding.edtPhoneClient.requestFocus()
//            } else {
//                activity?.let {
//                    Glide.with(it.baseContext).asBitmap().load(uriImagem).error(R.drawable.no_image)
//                        .apply(RequestOptions.overrideOf(800, 480))
//                        .listener(object : RequestListener<Bitmap> {
//
//                            override fun onLoadFailed(
//                                e: GlideException?,
//                                model: Any?,
//                                target: Target<Bitmap>?,
//                                isFirstResource: Boolean
//                            ): Boolean {
//                                Util.exibirToast(requireContext(), getString(R.string.error_reduced_image))
//                                dialogProgress.dismiss()
//                                return false
//                            }
//
//                            override fun onResourceReady(
//                                bitmap: Bitmap?,
//                                model: Any?,
//                                target: Target<Bitmap>?,
//                                dataSource: DataSource?,
//                                isFirstResource: Boolean
//                            ): Boolean {
//
//                                val baos = ByteArrayOutputStream()
//                                bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
//                                val data = baos.toByteArray()
//                                val reference =
//                                    firebaseStorage.reference
//                                        .child(dbClients)
//                                        .child("$pictureName.jpg")
//                                val uploadTask = reference.putBytes(data)
//                                uploadTask.continueWithTask { task ->
//                                    if (!task.isSuccessful) {
//                                        task.exception.let { it ->
//                                            throw it!!
//                                        }
//                                    }
//                                    reference.downloadUrl
//                                }.addOnSuccessListener { task ->
//                                    val url = task.toString()
//                                    validateData(url)
//                                }.addOnFailureListener { error ->
//                                    Util.exibirToast(
//                                        requireContext(),
//                                        getString(R.string.error_upload_image) + ":" + error.message.toString()
//                                    )
//                                    dialogProgress.dismiss()
//                                }
//                                return false
//                            }
//                        }).submit()
//                }
//            }
//        }
    }
    // endregion

    // region - FirebaseFirestore
    private fun validateData(url: String) {

        val phone = binding.edtPhoneClient.text.toString()
        val name = binding.edtNameClient.text.toString().trim()
        val birthday = binding.edtBirthdayClient.text.toString().trim()
        val email = binding.edtEmailClient.text.toString().trim().lowercase()

        clientModel = ClientModel()
        val date = Calendar.getInstance().time
        val dateTimeFormat = SimpleDateFormat(getString(R.string.type_date), Locale.getDefault())
        val clientDate = dateTimeFormat.format(date)

        clientModel.name = name.uppercase()
        clientModel.phone = phone
        clientModel.email = email
        clientModel.birthday = birthday
        clientModel.clientDate = clientDate
        clientModel.urlImagem = url
        insertClient()
    }

    // Fazer aqui !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


    private fun insertClient() {
        FirebaseFirestore.getInstance().collection(dbClients).document(clientModel.phone.toString())
            .set(clientModel).addOnCompleteListener {
                Util.exibirToast(requireContext(), getString(R.string.add_success_client))
                cleaner()
                dialogProgress.dismiss()
            }.addOnFailureListener {
                Util.exibirToast(requireContext(), getString(R.string.error_save_client))
                dialogProgress.dismiss()
            }
    }
    // endregion

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


    // region - FieldValidation
    private fun submitForm() {
        val name = checkEmptyField(binding.edtNameClient, binding.nameContainer, requireContext())
        cleanErrorValidation(binding.edtNameClient, binding.nameContainer)

        val phone = checkEmptyField(binding.edtPhoneClient, binding.phoneContainer, requireContext(), "phone")
        cleanErrorValidation(binding.edtPhoneClient, binding.phoneContainer)

        var email: Boolean = true

        if (!binding.edtEmailClient.text.isNullOrEmpty()) {
            email = checkEmptyField(binding.edtEmailClient, binding.emailContainer, requireContext(), "email")
        }
        cleanErrorValidation(binding.edtEmailClient, binding.emailContainer)

        if (name && phone && email) {
            if (uriImagem != null) {
                uploadImagem()
            } else {
                // SE NÃO TIVER IMAGEM  O URI E PREENCHIDO COM IMAGEM PADRÃO
                val drawable =
                    ContextCompat.getDrawable(requireContext(), R.drawable.no_image)
                val bitmap = drawable?.toBitmap()
                uriImagem = getImageUriFromBitmap(requireContext(), bitmap!!)
                uploadImagem()
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

    private fun showBottomSheetDialogCamera() {
        bottomSheetDialogCamera = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)

        val sheetBinding: ItemCustomBottonSheetTakePictureBinding =
            ItemCustomBottonSheetTakePictureBinding.inflate(layoutInflater, null, false)

        sheetBinding.clTakePhoto.setOnClickListener {
            checkPermissions()
        }

        sheetBinding.clGallery.setOnClickListener {
            obterImagemdaGaleria()
        }

        bottomSheetDialogCamera?.setContentView(sheetBinding.root)
        bottomSheetDialogCamera?.show()
    }

// endregion

    private fun configureComponents() {
        //Mask to Phone
        val country = PhoneNumberFormatType.PT_BR // OR PhoneNumberFormatType.PT_BR
        val phoneFormatter = PhoneMask(WeakReference(binding.edtPhoneClient), country)
        binding.edtPhoneClient.addTextChangedListener(phoneFormatter)
//        binding.edtPhoneClient.addTextChangedListener(DateMask.mask(binding.edtPhoneClient, DateMask.FORMAT_FONE))

        //Mask to Date
        binding.edtBirthdayClient.addTextChangedListener(
            DateMask.mask(
                binding.edtBirthdayClient,
                DateMask.FORMAT_DATE
            )
        )
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val uri = Uri.parse("android-app://com.clausfonseca.rosacha/client_fragment")
                    findNavController().navigate(uri)
                }
            })
    }

    private fun cleaner() {
        binding.apply {
            edtNameClient.text = null
            edtPhoneClient.text = null
            edtEmailClient.text = null
            edtBirthdayClient.text = null
            edtNameClient.requestFocus()
            binding.imvPhotoClient.setImageResource(R.drawable.no_image)
            binding.imvPlus.visibility = View.VISIBLE
        }
    }
}