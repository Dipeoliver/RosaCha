package com.clausfonseca.rosacha.view.dashboard.price

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentPriceBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetRequestPermissionBinding
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.extencionFunctions.hideKeyboard
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

class PriceFragment : Fragment() {

    private lateinit var binding: FragmentPriceBinding
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private var dbProducts: String = ""

    var price: Double = 0.00
    var qtyParcel: Int = 1


    private val db = FirebaseFirestore.getInstance()
    var bottomSheetDialogPermission: BottomSheetDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPriceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        firebaseStorage = Firebase.storage
        dbProducts = getString(R.string.db_product).toString()
        configureButton()
        initListeners()
        onBackPressed()
    }


    // BARCODE----------------------------------------------------------------------------
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                val Finalresult = result.contents
                binding.edtBarcodePrice.setText(Finalresult)
                binding.edtBarcodePrice.requestFocus()
                binding.edtBarcodePrice.selectAll()
                selectPrice(Finalresult)
            } else {
                binding.edtBarcodePrice.setText(getString(R.string.scan_failed))
                cleaner()
                binding.edtBarcodePrice.requestFocus()

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            binding.edtBarcodePrice.requestFocus()
            cleaner()
            binding.edtBarcodePrice.requestFocus()
        }
    }

    @Suppress("DEPRECATION")
    private fun configureButton() {

        binding.btnScanPrice.setOnClickListener {
            checkPermissions()
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
        val integrator: IntentIntegrator =
            IntentIntegrator.forSupportFragment(this@PriceFragment)
        integrator.setPrompt(getString(R.string.scan_active))
        integrator.initiateScan()
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
                            showBottomSheetDialogPermission()
                        }
                    }
                }

                PackageManager.PERMISSION_DENIED -> {
                    if (!shouldShowRequestPermissionRationale(permissions[0])) {
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


    // FIRESTORE-------------------------------------------------------------------------

    private fun selectPrice(barcode: String) {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")
        if (barcode != null && barcode.isNotEmpty()) {
            db.collection(dbProducts).document(barcode.toString()).get()
                .addOnSuccessListener { product ->
                    if (product != null && product.exists()) {
                        price = product.getDouble("salesPrice") ?: 0.0
                        binding.edtDescriptionPrice.setText(product.getString("description"))
                        binding.edtColorPrice.setText(product.getString("color"))
                        binding.edtSizePrice.setText(product.getString("size"))
                        binding.txtValuePrice.text = String.format("%.2f", price)
                        parcelCalc()

                        download_Image_Name(barcode)
                        dialogProgress.dismiss()

                    } else {
                        Util.exibirToast(requireContext(), getString(R.string.error_show_product))
                        dialogProgress.dismiss()
                        cleaner()
                    }
                }.addOnFailureListener { error ->
                    Util.exibirToast(requireContext(), getString(R.string.error_show_product) + ":" + error.message.toString())
                }
        } else {
            Util.exibirToast(requireContext(), getString(R.string.required_barcode_product))
            dialogProgress.dismiss()
            binding.edtBarcodePrice.requestFocus()
        }
    }
    // ----------------------------------------------------------------------------------

    // STORAGE---------------------------------------------------------------------------
    fun download_Image_Name(barcode: String) {
        val reference = firebaseStorage.reference.child(dbProducts).child(barcode + ".jpg")
//        val reference =
//            firebaseStorage!!.reference   // download de imagem adicionando usuario especifico
//                .child("imagens")
//                .child(uid.toString())
//                .child("uploadImagem.jpg")
        reference.downloadUrl.addOnSuccessListener { task ->
            val urlImage = task
            Glide.with(requireContext()).asBitmap().load(urlImage)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Util.exibirToast(requireContext(), getString(R.string.error_upload_image) + ":" + e.toString())
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
//                        Util.exibirToast(requireContext(), "Imagem carregada com Sucesso")
                        return false
                    }
                }).into(binding.imvProduct)

        }.addOnFailureListener { error ->
            Util.exibirToast(requireContext(), getString(R.string.error_upload_image) + ":" + error.message.toString())
        }
    }

    // ----------------------------------------------------------------------------------

    private fun initListeners() {
        binding.edtBarcodePrice.requestFocus()
        binding.btnSearchPrice.setOnClickListener {
            selectPrice(binding.edtBarcodePrice.text.toString())
            binding.btnSearchPrice.hideKeyboard()
        }

        binding.seekBar3.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                binding.txtQtyValue.text = ("${i}x")
                qtyParcel = i
                parcelCalc()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    private fun parcelCalc() {
        var result: Double = 0.0
        result = (price) / qtyParcel

        binding.txtParcelPriceValue.text = String.format("%.2f", result)
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val uri = Uri.parse("android-app://com.clausfonseca.rosacha/home_fragment")
                findNavController().navigate(uri)
            }
        })
    }

    private fun cleaner() {
        binding.edtDescriptionPrice.setText("")
        binding.txtValuePrice.text = ""
        binding.edtSizePrice.setText("")
        binding.edtColorPrice.setText("")
        binding.edtBarcodePrice.requestFocus()
        binding.edtBarcodePrice.selectAll()
        binding.imvProduct.setImageResource(R.drawable.baseline_image_not_supported_24)

    }
}