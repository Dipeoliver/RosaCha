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
import android.widget.Toast
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
import com.clausfonseca.rosacha.model.Client
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.mask.DateMask
import com.clausfonseca.rosacha.utils.mask.PhoneMask
import com.clausfonseca.rosacha.utils.mask.PhoneNumberFormatType
import com.clausfonseca.rosacha.utils.mask.validateEmailRegex
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
    private var selectedClient: Client? = null
    private var pictureName: String? = ""
    var uriImagem: Uri? = null
    var dialog: BottomSheetDialog? = null
    var dialogPermission: BottomSheetDialog? = null

    private val db = FirebaseFirestore.getInstance()

    var clientId: String? = null
    var oldId: String? = null
    var oldUrl: String = ""

    private var dbClients: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentClientEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedClient = EditClientFragmentArgs.fromBundle(requireArguments()).selectedClient

        // outro metodo de recuperar dados de outro fragment
//        selectedClient = requireArguments().getParcelable<Client>("client")

        firebaseStorage = Firebase.storage
        dbClients = getString(R.string.db_client)
        recoverClient()
        onBackPressed()
        initListeners()
        configureComponents()
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

                    binding.imvPhotoClientEdit.setImageURI(uriImagem)

                } else if (requestCode == 22 && uriImagem != null) {// camera

                    binding.imvPhotoClientEdit.setImageURI(uriImagem)
                } else {

                }
                dialog?.dismiss()
            }
        } else {
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
            val autorização = "com.clausfonseca.rosacha"
            val diretorio =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val path = diretorio.path ?: ""
            val nomeImagem = path + "/" + dbClients + pictureName + ".jpg"
            if (nomeImagem == "/" + dbClients + ".jpg") {
                val nomeImagem = diretorio.path + "/" + dbClients + System.currentTimeMillis() + ".jpg"
            }
            val file = File(nomeImagem)
            uriImagem = activity?.let { FileProvider.getUriForFile(it.baseContext, autorização, file) }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImagem)
        startActivityForResult(intent, 22)
    }

    // selecionar imagem da galeria
    private fun obterImagemdaGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, "Escolha uma Imagem"), 11)
    }

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
                        val reference =
                            firebaseStorage.reference
                                .child(dbClients)
                                .child(pictureName + ".jpg")
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
                                "Erro ao realizar o upload da imagem: ${error.message.toString()}"
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
            Util.exibirToast(requireContext(), "Falha ao deletar a imagem Antiga${error.message.toString()}")
        }
    }
    // ----------------------------------------------------------------------------------

    // FIRESTORE--------------------------------------------------------------------------
    private fun validateData(url: String) {

        val phone = binding.edtPhoneClientEdit.text.toString()
        val name = binding.edtNameClientEdit.text.toString().trim()
        val birthday = binding.edtBirthdayClientEdit.text.toString().trim()
        val email = binding.edtEmailClientEdit.text.toString().trim().lowercase()

        if (name.isNotEmpty() && phone.length > 13) {

            if (email != "" && !email.validateEmailRegex(email)) {
                Util.exibirToast(requireContext(), "Erro na validação do email")

            } else {
                selectedClient = Client()
                val date = Calendar.getInstance().time
                val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
                val clientDate = dateTimeFormat.format(date)

                selectedClient?.name = name.uppercase()
                selectedClient?.phone = phone
                selectedClient?.email = email
                selectedClient?.birthday = birthday
                selectedClient?.clientDate = clientDate
                selectedClient?.urlImagem = url
                selectedClient?.id = clientId

                if (oldId != phone) {
                    removeImage(oldId.toString())
                }

                updateClient(selectedClient!!)
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Preencher os campos Obrigatórios",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun updateClient(selectedClient: Client) {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")
        if (selectedClient != null) {

            val reference = db!!.collection(dbClients)

            val client = hashMapOf(
                // posso fazer update de apenas 1 campo se necessário
                "name" to selectedClient.name,
                "email" to selectedClient.email,
                "birthday" to selectedClient.birthday,
                "clientDate" to selectedClient.clientDate,
                "urlImagem" to selectedClient.urlImagem
            )
            reference.document(selectedClient.phone.toString()).update(client as Map<String, Any>).addOnSuccessListener {
                Util.exibirToast(requireContext(), "Update com Sucesso ao salvar os dados")
                dialogProgress.dismiss()
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/client_fragment")
                findNavController().navigate(uri)
            }.addOnFailureListener { error ->
                dialogProgress.dismiss()
                Util.exibirToast(requireContext(), "erro ao salvar no banco ${error.message.toString()}")
            }
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
                            dialog?.dismiss()
                            showBottomSheetDialogPermission()
                        }
                    }
                }

                PackageManager.PERMISSION_DENIED -> {
                    if (!shouldShowRequestPermissionRationale(permissions[0])) {
                        dialog?.dismiss()
                        showBottomSheetDialogPermission()
                    }
                }
            }
        }
    }

    private fun showBottomSheetDialogPermission() {
        dialogPermission = BottomSheetDialog(requireContext())
        val sheetBinding: ItemCustomBottonSheetRequestPermissionBinding =
            ItemCustomBottonSheetRequestPermissionBinding.inflate(layoutInflater, null, false)

        sheetBinding.btnCancel.setOnClickListener {
            dialogPermission?.dismiss()
        }

        sheetBinding.btnConfig.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", requireActivity().packageName, null)
            intent.data = uri
            requireContext().startActivity(intent)
            dialogPermission?.dismiss()
        }

        dialogPermission?.setContentView(sheetBinding.root)
        dialogPermission?.show()
    }

    // ----------------------------------------------------------------------------------------------------------------


    private fun initListeners() {
        binding.btnUpdateClient.setOnClickListener {

            if (uriImagem == null && binding.imvPhotoClientEdit.getBackground() != null) {
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

        binding.imvPhotoClientEdit.setOnClickListener {
            if (binding.edtPhoneClientEdit.text.isNotEmpty()) showBottomSheetDialog()
            else Util.exibirToast(requireContext(), "Preencher campo Telefone Primeiro")
        }

        binding.btnBackEdit.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/client_fragment")
            findNavController().navigate(uri)
        }
    }

    // para corrigir problema de falta de imagem selecionada
    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

    private fun showBottomSheetDialog() {
        dialog = BottomSheetDialog(requireContext())

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
        dialog?.setContentView(sheetBinding.root)
        dialog?.show()
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/client_fragment")
                findNavController().navigate(uri)
            }
        })
    }

    private fun recoverClient() {
        binding.edtNameClientEdit.setText(selectedClient?.name.toString())
        binding.edtPhoneClientEdit.setText(selectedClient?.phone.toString())
        binding.edtBirthdayClientEdit.setText(selectedClient?.birthday.toString())
        binding.edtEmailClientEdit.setText(selectedClient?.email.toString())
        clientId = selectedClient?.id

        oldId = selectedClient?.phone.toString()

        var url = selectedClient?.urlImagem
        oldUrl = selectedClient?.urlImagem.toString()

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

    private fun configureComponents() {
        //Mask to Phone
        val country = PhoneNumberFormatType.PT_BR // OR PhoneNumberFormatType.PT_BR
        val phoneFormatter = PhoneMask(WeakReference(binding.edtPhoneClientEdit), country)
        binding.edtPhoneClientEdit.addTextChangedListener(phoneFormatter)
//        binding.edtPhoneClient.addTextChangedListener(DateMask.mask(binding.edtPhoneClient, DateMask.FORMAT_FONE))

        //Mask to Date
        binding.edtBirthdayClientEdit.addTextChangedListener(DateMask.mask(binding.edtBirthdayClientEdit, DateMask.FORMAT_DATE))
    }
}