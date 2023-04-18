package com.clausfonseca.rosacha.view.dashboard.client

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
import com.clausfonseca.rosacha.databinding.FragmentClientAddBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetRequestPermissionBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetTakePictureBinding
import com.clausfonseca.rosacha.model.Client
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.mask.DateMask
import com.clausfonseca.rosacha.utils.mask.PhoneMask
import com.clausfonseca.rosacha.utils.mask.PhoneNumberFormatType
import com.clausfonseca.rosacha.utils.mask.validateEmailRegex
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class AddClientFragment : Fragment() {

    private lateinit var binding: FragmentClientAddBinding
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var client: Client
    private var dbClients: String = ""
    private val viewModel: AddClientViewModel by viewModels()
    private var pictureName: String? = ""
    var uriImagem: Uri? = null
    var bottomSheetDialogPermission: BottomSheetDialog? = null
    var bottomSheetDialogCamera: BottomSheetDialog? = null
    val dialogProgress = DialogProgress()
    var email: String = ""

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
        binding.edtNameClient.requestFocus()
        firebaseStorage = Firebase.storage
        dbClients = getString(R.string.db_client)
        initListeners()
        configureComponents()

        // ao clicar botão voltar abaixo
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val uri = Uri.parse("android-app://com.clausfonseca.rosacha/client_fragment")
                    findNavController().navigate(uri)
                }
            })
    }


    // IMAGEVIEW   --------------------------------------------------------
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
    // -----------------------------------------------------------------------


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

    // selecionar imagem da galeria
    private fun obterImagemdaGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 11)
    }

    // com recurso para diminuir a imagem
    fun uploadImagem() {
        dialogProgress.show(childFragmentManager, "0")
        pictureName = binding.edtPhoneClient.text.toString()

        val reference = viewModel.db.collection("@string/").document(pictureName.toString())
        reference.get().addOnSuccessListener { item ->
            if (item.exists()) {
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

                                val baos = ByteArrayOutputStream()
                                bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                                val data = baos.toByteArray()
                                val reference =
                                    firebaseStorage.reference
                                        .child(dbClients)
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

        val phone = binding.edtPhoneClient.text.toString()
        val name = binding.edtNameClient.text.toString().trim()
        val birthday = binding.edtBirthdayClient.text.toString().trim()
        val email = binding.edtEmailClient.text.toString().trim().lowercase()

        client = Client()
        val date = Calendar.getInstance().time
        val dateTimeFormat = SimpleDateFormat(getString(R.string.type_date), Locale.getDefault())
        val clientDate = dateTimeFormat.format(date)

        client.name = name.uppercase()
        client.phone = phone
        client.email = email
        client.birthday = birthday
        client.clientDate = clientDate
        client.urlImagem = url
        insertClient()
    }

    private fun insertClient() {
        viewModel.db.collection(dbClients).document(client.phone.toString())
            .set(client).addOnCompleteListener {
                Util.exibirToast(requireContext(), getString(R.string.add_success_client))
                cleaner()
                dialogProgress.dismiss()
            }.addOnFailureListener {
                Util.exibirToast(requireContext(), getString(R.string.error_save_client))
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
                REQUEST_PERMISSION_CODE
            )
            return
        }
        obterImagemdaCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
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

    // ----------------------------------------------------------------------------------------------------------------

    private fun initListeners() {
        binding.btnAddClient.setOnClickListener {

            email = binding.edtEmailClient.text.toString().trim().lowercase()

            if (binding.edtNameClient.text.isNotEmpty()
                && binding.edtPhoneClient.text.length > 13
            ) {
                if (email != "" && !email.validateEmailRegex(email)) {
                    Util.exibirToast(requireContext(), getString(R.string.invalid_email_register_fragment))
                } else {
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
            } else {
                Util.exibirToast(requireContext(), getString(R.string.required_fields))
            }
        }

        binding.imvPhotoClient.setOnClickListener {
            if (binding.edtPhoneClient.text.isNotEmpty()) showBottomSheetDialogCamera()
            else Util.exibirToast(requireContext(), getString(R.string.required_phone_client))
        }

        binding.btnBack.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/client_fragment")
            findNavController().navigate(uri)
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

    private fun showBottomSheetDialogCamera() {
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

    private fun cleaner() {
        binding.apply {
            edtNameClient.text.clear()
            edtPhoneClient.text.clear()
            edtEmailClient.text.clear()
            edtBirthdayClient.text.clear()
            edtNameClient.requestFocus()
            binding.imvPhotoClient.setImageResource(R.drawable.no_image)
            binding.imvPlus.visibility = View.VISIBLE
        }
    }
}