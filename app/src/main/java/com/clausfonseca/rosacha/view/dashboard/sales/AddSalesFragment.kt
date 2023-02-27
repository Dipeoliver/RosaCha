package com.clausfonseca.rosacha.view.dashboard.sales

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.clausfonseca.rosacha.databinding.FragmentSalesAddBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetRequestPermissionBinding
import com.clausfonseca.rosacha.model.ItensSales
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.mask.PhoneMask
import com.clausfonseca.rosacha.utils.mask.PhoneNumberFormatType
import com.clausfonseca.rosacha.utils.pdf.PDFConverter
import com.clausfonseca.rosacha.utils.pdf.PdfDetails
import com.clausfonseca.rosacha.view.adapter.ItensSalesAdapter
import com.clausfonseca.rosacha.view.dashboard.client.AddClientFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*


class AddSalesFragment : Fragment() {

    private lateinit var binding: FragmentSalesAddBinding
    private lateinit var firebaseStorage: FirebaseStorage
    private val itensSales = mutableListOf<ItensSales>()
    private lateinit var itensSalesAdapter: ItensSalesAdapter
    private val db = FirebaseFirestore.getInstance()
    val dialogProgress = DialogProgress()
    var dialogPermission: BottomSheetDialog? = null
    var barcode: String? = ""
    var soma: Double = 0.0
    var qtyParcel: Int = 1

    var lista = mutableListOf<String>()

    var MIN = 0
    var MAX = 25
    var STEP = 5
    var progress_custom: Int = 0
    var finalPrice: Double = 0.0


    var clientName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSalesAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBackPressed()
        initListeners()
        initAdapter()
        configureComponents()
    }

    // BARCODE     --------------------------------------------------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // BARCODE
        var result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                barcode = result.contents
                getItem(barcode.toString())
            } else {
                // criar um dialog aqui
                binding.edtBarcode.setText("scan failed")
                binding.edtBarcode.requestFocus()
                binding.edtBarcode.selectAll()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            // criar um dialog aqui
            binding.edtBarcode.requestFocus()
            binding.edtBarcode.selectAll()
        }
    }
    // --------------------------------------------------------------------


    private fun initListeners() {
        binding.edtBarcode.requestFocus()

        binding.btnScan.setOnClickListener {
            checkPermissions()
        }

        binding.btnSearchClient.setOnClickListener {
            if (binding.edtPhoneClient.text.toString().isNotEmpty()) {
                getClient(binding.edtPhoneClient.text.toString())
            } else {
                Util.exibirToast(requireContext(), "Campo Telefone não pode estar em Branco")

            }
        }

        binding.btnPriceSearch.setOnClickListener {
            if (binding.edtBarcode.text.toString().isNotEmpty()) {
                getItem(binding.edtBarcode.text.toString())
            } else {
                Util.exibirToast(requireContext(), "Campo Barcode não pode estar em Branco")
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                binding.txtQuantity.text = ("${i}x")
                qtyParcel = i
                parcelCalc()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        // mudar o step de 5 em 5
        binding.seekBar2.max = (MAX - MIN) / STEP
        binding.seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                progress_custom = MIN + (i * STEP)
                binding.txtPercentage.text = ("$progress_custom%")
                discountCalc()
                parcelCalc()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        binding.btnAddSales.setOnClickListener {

            val date = Calendar.getInstance().time
            val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val actualDate = dateTimeFormat.format(date)

            Log.d(
                "Final_Sales",
                "${clientName}, ${actualDate}, ${soma}, ${finalPrice}, ${soma - finalPrice}, ${itensSales.size}"
            )

            val invoiceNumber: String = "00001"

            // chamada para gerar o PDF
            val pdfDetails = PdfDetails(invoiceNumber, clientName, actualDate, soma, soma - finalPrice, finalPrice, itensSales)
            val pdfConverter = PDFConverter()
            pdfConverter.createPdf(requireContext(), pdfDetails, requireActivity())

        }
    }


    private fun parcelCalc() {
        var parcel: Double = 0.0
        parcel = finalPrice / qtyParcel
        binding.txtParcelValue.text = String.format("%.2f", parcel)
    }

    private fun discountCalc() {
        finalPrice = (soma - (soma * (progress_custom.toDouble() / 100)))
        binding.txtFinalPrice.text = String.format("%.2f", finalPrice)
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val uri = Uri.parse("android-app://com.clausfonseca.rosacha/home_fragment")
                    findNavController().navigate(uri)
                }
            })
    }


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
            IntentIntegrator.forSupportFragment(this@AddSalesFragment)
        integrator.setPrompt("Scanner RosaCha Ativo")
        integrator.initiateScan()
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

// --------------------------------------------------------------------


    // FIRESTORE ________________________________

    fun getItem(barcode: String) {

        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        db!!.collection("Products").document(barcode).get().addOnSuccessListener { task ->

            dialogProgress.dismiss()
            if (task != null && task.exists()) {
//                val key = task.id // pegar o nome  da pasta do documento
                val dados = task.data
                val item = task.toObject(ItensSales::class.java)
                if (item != null) {
                    itensSales.add(item)
                    lista.add(item.toString())
                    itensSalesAdapter.notifyDataSetChanged()
                    soma = 0.0
                    itensSales.forEach {
                        soma += it.salesPrice ?: 0.0

                    }

                    binding.txtTotalPrice.text = String.format("%.2f", soma)
                    discountCalc()

                    binding.edtBarcode.setText("")
                    binding.edtBarcode.requestFocus()

                    parcelCalc()
                }
            } else {
                binding.edtBarcode.requestFocus()
                binding.edtBarcode.selectAll()

                Util.exibirToast(requireContext(), "Erro ao exibir o Item, ele não existe")
            }
        }.addOnFailureListener { error ->
            dialogProgress.dismiss()
            "Erro de comunicação com servidor ${error.message.toString()}"
        }
    }

    fun getClient(client: String) {

        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        db!!.collection("Clients").document(client).get().addOnSuccessListener { task ->

            dialogProgress.dismiss()
            if (task != null && task.exists()) {
//                val key = task.id // pegar o nome  da pasta do documento
                val dados = task.data
                val item = task.toObject(ItensSales::class.java)
                if (item != null) {
                    clientName = dados?.get("name").toString()
                    binding.txtClient.text = clientName
                }
            } else {
                Util.exibirToast(requireContext(), "Erro ao exibir o Cliente, ele não existe")
            }
        }.addOnFailureListener { error ->
            dialogProgress.dismiss()
            "Erro de comunicação com servidor ${error.message.toString()}"
        }
    }


// --------------------------------------------------------------------

    private fun initAdapter() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setHasFixedSize(true)
        itensSalesAdapter = ItensSalesAdapter(requireContext(), itensSales) { item ->
            // ao apagar um item da lista executa abaixo
            soma = 0.0
            item.forEach {
                soma += it.salesPrice ?: 0.0
            }
            binding.txtTotalPrice.text = String.format("%.2f", soma)
            discountCalc()
            parcelCalc()
        }
        binding.recyclerView.adapter = itensSalesAdapter
    }

    private fun configureComponents() {
        //Mask to Phone
        val country = PhoneNumberFormatType.PT_BR // OR PhoneNumberFormatType.PT_BR
        val phoneFormatter = PhoneMask(WeakReference(binding.edtPhoneClient), country)
        binding.edtPhoneClient.addTextChangedListener(phoneFormatter)
    }
}