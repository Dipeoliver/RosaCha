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
import com.clausfonseca.rosacha.databinding.FragmentClientEditBinding
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
import com.clausfonseca.rosacha.utils.mask.validateEmailRegex
import com.clausfonseca.rosacha.view.dashboard.client.addClient.AddClientFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class EditClientFragment : Fragment() {

    private lateinit var binding: FragmentClientEditBinding
    private lateinit var firebaseStorage: FirebaseStorage
    private var selectedClientModel: ClientModel? = null
    private var pictureName: String? = ""
    private val db = FirebaseFirestore.getInstance()
    private var dbClients: String = ""
    var uriImagem: Uri? = null
    var bottomSheetDialogCamera: BottomSheetDialog? = null
    var bottomSheetDialogPermission: BottomSheetDialog? = null
    var clientId: String? = null
    var oldId: String? = null
    var oldUrl: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentClientEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedClientModel = EditClientFragmentArgs.fromBundle(requireArguments()).selectedClient

        // outro metodo de recuperar dados de outro fragment
        // selectedClientModel = requireArguments().getParcelable<ClientModel>("client")

        firebaseStorage = Firebase.storage
        dbClients = getString(R.string.db_client)
        recoverClient()
        initListeners()
        configureComponents()
        onBackPressed()
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

                    binding.imvPhotoClientEdit.setImageURI(uriImagem)

                } else if (requestCode == 22 && uriImagem != null) {// camera

                    binding.imvPhotoClientEdit.setImageURI(uriImagem)
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
            val authorisation = "com.clausfonseca.rosacha"
            val directory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val path = directory.path ?: ""
            val imageName = path + "/" + dbClients + pictureName + ".jpg"
            if (imageName == "/" + dbClients + ".jpg") {
                val imageName = directory.path + "/" + dbClients + System.currentTimeMillis() + ".jpg"
            }
            val file = File(imageName)
            uriImagem = activity?.let { FileProvider.getUriForFile(it.baseContext, authorisation, file) }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImagem)
        startActivityForResult(intent, 22)
    }

    // selecionar imagem da galeria
    private fun obterImagemdaGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 11)
    }

    // para corrigir problema de falta de imagem selecionada
    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

    // endregion

    // region - FirebaseStorage
    // com recurso para diminuir a imagem
    private fun uploadImagem() {

        pictureName = binding.edtPhoneClientEdit.text.toString()
        activity?.let {
            Glide.with(it.baseContext).asBitmap().load(uriImagem).error(R.drawable.no_image)
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
                        }
                        return false
                    }
                }).submit()
        }
    }

    //remover imagem antiga da galeria
    private fun removeImage(id: String) {
        val reference = firebaseStorage.reference.child(dbClients).child("${id}.jpg")
        reference.delete().addOnSuccessListener { task ->
        }.addOnFailureListener { error ->
            Util.exibirToast(requireContext(), getString(R.string.error_delete_image) + error.message.toString())
        }
    }
//endregion

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

                if (oldId != phone) {
                    removeImage(oldId.toString())
                }

                updateClient(selectedClientModel!!)
            }
        } else {
            Util.exibirToast(requireContext(), getString(R.string.required_fields))
        }

    }

    private fun updateClient(selectedClientModel: ClientModel) {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")
        if (selectedClientModel != null) {

            val reference = db!!.collection(dbClients)

            val client = hashMapOf(
                // posso fazer update de apenas 1 campo se necessário
                "name" to selectedClientModel.name,
                "email" to selectedClientModel.email,
                "birthday" to selectedClientModel.birthday,
                "clientDate" to selectedClientModel.clientDate,
                "urlImagem" to selectedClientModel.urlImagem
            )
            reference.document(selectedClientModel.phone.toString()).update(client as Map<String, Any>).addOnSuccessListener {
                Util.exibirToast(requireContext(), getString(R.string.update_data))
                dialogProgress.dismiss()
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/client_fragment")
                findNavController().navigate(uri)
            }.addOnFailureListener { error ->
                dialogProgress.dismiss()
                Util.exibirToast(requireContext(), getString(R.string.error_update_data_client) + ":" + error.message.toString())
            }
        }
    }

    private fun recoverClient() {
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

//endregion

    // region - FieldValidation


    private fun submitForm() {
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
            if (uriImagem == null && binding.imvPhotoClientEdit.background != null) {
                // SE NÃO TIVER IMAGEM  O URI E PREENCHIDO COM IMAGEM PADRÃO
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.no_image)
                val bitmap = drawable?.toBitmap()
                uriImagem = getImageUriFromBitmap(requireContext(), bitmap!!)
                uploadImagem()

            } else if (uriImagem != null) {
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
            obterImagemdaGaleria()
        }
        bottomSheetDialogCamera?.setContentView(sheetBinding.root)
        bottomSheetDialogCamera?.show()
    }
// endregion


    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/client_fragment")
                findNavController().navigate(uri)
            }
        })
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
}