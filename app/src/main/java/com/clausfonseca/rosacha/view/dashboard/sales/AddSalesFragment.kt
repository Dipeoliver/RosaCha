package com.clausfonseca.rosacha.view.dashboard.sales

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentSalesAddBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetAfterSalesBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetRequestPermissionBinding
import com.clausfonseca.rosacha.model.AddSales
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

@Suppress("UNNECESSARY_SAFE_CALL")
class AddSalesFragment : Fragment() {

    private lateinit var binding: FragmentSalesAddBinding
    private lateinit var addSales: AddSales
    private lateinit var itensSalesAdapter: ItensSalesAdapter
    private lateinit var auth: FirebaseAuth
    private val itensSales = mutableListOf<ItensSales>()
    private val db = FirebaseFirestore.getInstance()
    private var dbClients: String = ""
    private var dbProducts: String = ""
    private var dbSales: String = ""


    var dialogAfterSales: BottomSheetDialog? = null
    var dialogPermission: BottomSheetDialog? = null
    val dialogProgress = DialogProgress()
    var barcode: String? = ""
    var soma: Double = 0.0
    var qtyParcel: Int = 1
    var paid = ""
    var moneyPaid: Double = 0.0

    var client: String = ""


    var MIN = 0
    var MAX = 25
    var STEP = 5
    var progress_custom: Int = 0
    var finalPrice: Double = 0.0

    var actualDate: String = ""
    var invoiceNumber: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSalesAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        dbClients = getString(R.string.db_client).toString()
        dbProducts = getString(R.string.db_product).toString()
        dbSales = getString(R.string.db_sales).toString()

        onBackPressed()
        initListeners()
        initAdapter()
    }

    override fun onResume() {
        super.onResume()
        cleanner()
    }

    private fun initListeners() {
        binding.edtBarcode.requestFocus()

        binding.btnScan.setOnClickListener {
            checkPermissions()
        }

        binding.btnSearchClient.setOnClickListener {
            getUserDialog()
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
            if (itensSales.size == 0) {

                Util.exibirToast(requireContext(), "Adicionar pelo menos um item a ser vendido")
            } else {
                if (finalPrice < 0) {
                    Util.exibirToast(requireContext(), "O valor da venda tem de ser positivo, verificar valor Pago")
                } else {

                    informationDialog()
                }
            }
        }

        binding.edtPaid.setOnKeyListener(View.OnKeyListener { v, KeyCode, event ->
            if (KeyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                valuePaid()
                return@OnKeyListener true
            }
            false
        })
    }

    // calculo de variações de valores -----------------------------------------
    private fun parcelCalc() {
        var parcel: Double = 0.0
        parcel = (finalPrice) / qtyParcel
        binding.txtParcelValue.text = String.format("%.2f", parcel)
    }

    private fun discountCalc() {
        finalPrice = (soma - (soma * (progress_custom.toDouble() / 100))) - moneyPaid
        binding.txtDiscountValue.setText(String.format("%.2f", (soma * (progress_custom.toDouble() / 100))))
        binding.txtFinalPrice.text = String.format("%.2f", finalPrice)
    }

    private fun valuePaid() {
        paid = binding.edtPaid.text.toString()

        fun String.fullTrim() = trim().replace("\uFEFF", "")
        moneyPaid = paid.fullTrim().toDouble()

        finalPrice = soma - moneyPaid

        binding.txtFinalPrice.setText(finalPrice.toString())
        discountCalc()
        parcelCalc()

        binding.btnAddSales.requestFocus()
    }

    // -------------------------------- -----------------------------------------
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


    // CAMERA PERMISSION ---------------------
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


    // BARCODE     ------------------------------
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

    private fun insertSales() {
        dialogProgress.show(childFragmentManager, "0")

        val date = Calendar.getInstance().time
        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        actualDate = dateTimeFormat.format(date)

        invoiceNumber =
            dateTimeFormat.format(date)
                .replace("/", "")
                .replace(":", "")
                .replace(" ", "")

        addSales = AddSales()
        addSales.id = invoiceNumber
        addSales.price = soma
        addSales.discount = soma - finalPrice
        addSales.totalPrice = finalPrice
        addSales.client = binding.txtClient.text.toString().uppercase()
        addSales.salesOwner = auth.currentUser?.email
        addSales.salesDate = actualDate
        addSales.itens = itensSales


        db.collection(dbSales).document(invoiceNumber)
            .set(addSales).addOnCompleteListener {
                showBottomSheetDialogAfterSales()
                dialogProgress.dismiss()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao adicionar Venda", Toast.LENGTH_SHORT)
                    .show()
                dialogProgress.dismiss()
            }
    }

    // FIRESTORE ________________________________

    private fun getItem(barcode: String) {

        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        db!!.collection(dbProducts).document(barcode).get().addOnSuccessListener { task ->

            dialogProgress.dismiss()
            if (task != null && task.exists()) {
//                val key = task.id // pegar o nome  da pasta do documento
                val dados = task.data
                val item = task.toObject(ItensSales::class.java)
                if (item != null) {
                    itensSales.add(item)
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

    private fun getClient(client: String) {

        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        db!!.collection(dbClients).document(client).get().addOnSuccessListener { task ->

            dialogProgress.dismiss()
            if (task != null && task.exists()) {
//                val key = task.id // pegar o nome  da pasta do documento
                val dados = task.data
                val item = task.toObject(ItensSales::class.java)
                if (item != null) {

                    this.client = dados?.get("name").toString()
                    Log.d("GETCLIENT", this.client)
//                    binding.txtClient.setText("")
//                    binding.txtClient.setText(dados?.get("name").toString())
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

    private fun informationDialog() {

        val builder = AlertDialog.Builder(requireContext())

        //set title for alert dialog
        builder.setTitle(Html.fromHtml("<font color='#FB2391'>Atenção</font>"));
//        builder.setTitle("Atenção")

        //set message for alert dialog
        builder.setMessage("Realmente deseja Finalizar a Venda?")
        builder.setIcon(R.drawable.ic_rosa_round)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            insertSales()
        }
//        //performing cancel action
//        builder.setNeutralButton("Cancel"){dialogInterface , which ->
//            Toast.makeText(applicationContext,"clicked cancel\n operation cancel",Toast.LENGTH_LONG).show()
//        }
        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->

            // AÇÂO PARA O NÂO

        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun getUserDialog() {

        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.item_dialog_get_client, null)
//        dialogLayout.setBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.color.rose, null))
        val editText = dialogLayout.findViewById<EditText>(R.id.edt_client_phone)
        var result = dialogLayout.findViewById<TextView>(R.id.txt_result)
        val button = dialogLayout.findViewById<ImageButton>(R.id.btn_search1)

        editText.requestFocus()
        //Mask to Phone
        val country = PhoneNumberFormatType.PT_BR // OR PhoneNumberFormatType.PT_BR
        val phoneFormatter = PhoneMask(WeakReference(editText), country)
        editText.addTextChangedListener(phoneFormatter)

        button.setOnClickListener {
            getClient(editText.text.toString())
            result.text = this.client
        }

        with(builder) {
            setTitle(Html.fromHtml("<font color='#FB2391'>Pesquisar Clientes</font>"));
            setPositiveButton("OK") { dialog, which ->
                binding.txtClient.setText(client)
            }
            setNegativeButton("Cancel") { dialog, which ->

            }
            setView(dialogLayout)
            show()
        }
    }

    private fun showBottomSheetDialogAfterSales() {
        dialogAfterSales = BottomSheetDialog(requireContext())

        val sheetBinding: ItemCustomBottonSheetAfterSalesBinding =
            ItemCustomBottonSheetAfterSalesBinding.inflate(layoutInflater, null, false)

        sheetBinding.imvBottomNewSales.setOnClickListener {
            dialogAfterSales?.dismiss()
            cleanner()
        }
        sheetBinding.txtBottomNewSales.setOnClickListener {
            dialogAfterSales?.dismiss()
            cleanner()
        }

        sheetBinding.imvBottomListSales.setOnClickListener {
            // link para lista de vendas
            Util.exibirToast(requireContext(), "Ir para a lista de vendas")
        }
        sheetBinding.txtBottomListSales.setOnClickListener {
            // link para lista de vendas
            Util.exibirToast(requireContext(), "Ir para a lista de vendas")
        }

        sheetBinding.imvBottomPdfSales.setOnClickListener {
            creatPdf()
            dialogAfterSales?.dismiss()
            cleanner()
        }
        sheetBinding.txtBottomPdfSales.setOnClickListener {
            creatPdf()
            dialogAfterSales?.dismiss()
            cleanner()
        }
        dialogAfterSales?.setContentView(sheetBinding.root)
        dialogAfterSales?.setCancelable(false)
        dialogAfterSales?.show()
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

    private fun creatPdf() {
        // chamada para gerar o PDF

        val pdfDetails = PdfDetails(
            invoiceNumber,
            binding.txtClient.text.toString().uppercase(),
            actualDate,
            soma,
            soma * (progress_custom.toDouble() / 100),
            finalPrice,
            moneyPaid,
            itensSales
        )
        val pdfConverter = PDFConverter()
        pdfConverter.createPdf(requireContext(), pdfDetails, requireActivity())
    }

    private fun cleanner() {
        binding.seekBar.progress = 0
        binding.seekBar2.progress = 0
        binding.edtPaid.setText("")
        binding.txtTotalPrice.text = "0.00"
        binding.txtFinalPrice.text = "0.00"
        binding.txtParcelValue.text = "0.00"
        binding.txtClient.setText("")
        itensSales.clear()
        itensSalesAdapter.notifyDataSetChanged()
        binding.edtBarcode.requestFocus()
    }
}