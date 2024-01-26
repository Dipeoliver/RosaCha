package com.clausfonseca.rosacha.view.dashboard.sales

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentSalesAddBinding
import com.clausfonseca.rosacha.databinding.ItemCustomBottonSheetRequestPermissionBinding
import com.clausfonseca.rosacha.model.ItensSales
import com.clausfonseca.rosacha.model.Product
import com.clausfonseca.rosacha.model.Sales
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.extencionFunctions.checkEmptyField
import com.clausfonseca.rosacha.utils.extencionFunctions.cleanErrorValidation
import com.clausfonseca.rosacha.utils.mask.PhoneMask
import com.clausfonseca.rosacha.utils.mask.PhoneNumberFormatType
import com.clausfonseca.rosacha.view.adapter.ItensSalesAdapter
import com.clausfonseca.rosacha.view.dashboard.client.addClient.AddClientFragment
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
import kotlin.math.roundToInt

@Suppress("UNNECESSARY_SAFE_CALL")
class AddSalesFragment : Fragment() {

    private lateinit var binding: FragmentSalesAddBinding
    private lateinit var addSales: Sales
    private lateinit var itemsSalesAdapter: ItensSalesAdapter
    private lateinit var auth: FirebaseAuth
    private val itemsSales = mutableListOf<ItensSales>()
    private val db = FirebaseFirestore.getInstance()
    private var dbClients: String = ""
    private var dbProducts: String = ""
    private var dbSales: String = ""
    private var dbMonthSales: String = ""
    private var soma: Double = 0.0
    private var qtyParcel: Int = 1
    private var paid = ""
    private var moneyPaid: Double = 0.0
    private var discount: Double = 0.00
    private var parcelValue: Double = 0.00
    private var seekMin = 0
    private var seekMax = 25
    private var seekStep = 5
    private var progressCustom: Int = 0
    private var finalPrice: Double = 0.0
    private var actualDate: String = ""
    private var invoiceNumber: String = ""
    private var barcode: String? = ""
    private var client: String = ""
    private val dialogProgress = DialogProgress()
    private var bottomSheetDialogPermission: BottomSheetDialog? = null
    private val calendar = Calendar.getInstance()
    var qty = 1

    var mes = ""
    var actualvalue = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSalesAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        dbClients = getString(R.string.db_client)
        dbProducts = getString(R.string.db_product)
        dbSales = getString(R.string.db_sales)
        dbMonthSales = getString(R.string.db_month_sales)

        onBackPressed()
        initListeners()
        initAdapter()
        updateQuantity()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // BARCODE
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                barcode = result.contents
                getItem(barcode.toString())
            } else {
                // criar um dialog aqui
                binding.edtBarcode.setText(getString(R.string.scan_failed))
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

    private fun submitForm() {
        val password = checkEmptyField(binding.edtBarcode, binding.barcodeContainer, requireContext())
        cleanErrorValidation(binding.edtBarcode, binding.barcodeContainer)
        if (password) {
            getItem(binding.edtBarcode.text.toString())
        }
    }

    private fun initListeners() {
//        binding.edtBarcode.requestFocus()

        binding.btnQtyAdd.setOnClickListener {
            getQuantityDialog()
        }
        binding.btnScan.setOnClickListener {
            checkPermissions()
        }

        binding.btnPriceSearch.setOnClickListener {
            submitForm()
        }
        binding.btnSearchClient.setOnClickListener {
            getUserDialog()

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
        binding.seekBar2.max = (seekMax - seekMin) / seekStep
        binding.seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                progressCustom = seekMin + (i * seekStep)
                binding.txtPercentage.text = ("$progressCustom%")
                discountCalc()
                parcelCalc()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        binding.btnAddSales.setOnClickListener {
            if (itemsSales.size == 0) {
                Util.exibirToast(requireContext(), getString(R.string.add_item_sales))
            } else {
                if (finalPrice < 0) {
                    Util.exibirToast(requireContext(), getString(R.string.check_value_sales))
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        informationDialog()
                    }
                }
            }
        }

        binding.edtPaid.setOnKeyListener(View.OnKeyListener { _, KeyCode, event ->
            if (KeyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                valuePaid()
                return@OnKeyListener true
            }
            false
        })

        binding.btnBack.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/sales_fragment")
            findNavController().navigate(uri)
        }
    }

    // calculo de variações de valores -----------------------------------------
    private fun parcelCalc() {
        parcelValue = (finalPrice) / qtyParcel
        binding.txtParcelValue.text = String.format("%.2f", parcelValue)
    }

    private fun discountCalc() {
        discount = (soma * (progressCustom.toDouble() / 100))
        finalPrice = (soma - discount) - moneyPaid
        binding.txtDiscountValue.text = String.format("%.2f", discount)
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
                    val uri = Uri.parse("android-app://com.clausfonseca.rosacha/sales_fragment")
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
        integrator.setPrompt(getString(R.string.scan_active))
        integrator.initiateScan()
    }
    // BARCODE     ------------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    private fun insertSales() {
        dialogProgress.show(childFragmentManager, "0")

        val date = Calendar.getInstance().time
        val dateTimeFormat = SimpleDateFormat(getString(R.string.type_date), Locale.getDefault())
        actualDate = dateTimeFormat.format(date)
        invoiceNumber =
            dateTimeFormat.format(date)
                .replace("/", "")
                .replace(":", "")
                .replace(" ", "")

        addSales = Sales()
        addSales.id = invoiceNumber
        addSales.price = (soma * 100.0).roundToInt() / 100.0
        addSales.discount = ((discount) * 100.0).roundToInt() / 100.0
        addSales.paid = (moneyPaid * 100.0).roundToInt() / 100.0
        addSales.totalPrice = ((soma - discount) * 100.0).roundToInt() / 100.0
        addSales.client = binding.txtClient.text.toString().uppercase()
        addSales.salesOwner = auth.currentUser?.email
        addSales.salesDate = actualDate
        addSales.month = calendar.get(Calendar.MONTH) + 1
        addSales.year = calendar.get(Calendar.YEAR)
        addSales.itens = itemsSales
        addSales.qtyParcel = qtyParcel
        addSales.parcelDate = calendar.get(Calendar.DAY_OF_MONTH)
        addSales.parceled = (parcelValue * 100.0).roundToInt() / 100.0

        db.collection(dbSales).document(invoiceNumber)
            .set(addSales).addOnCompleteListener {
                dialogProgress.dismiss()
                val args = Bundle()
                args.putString("id", invoiceNumber)
                updateStock()
                findNavController().navigate(R.id.action_addSalesFragment_to_afterSalesFragment, args)
            }.addOnFailureListener {
                Util.exibirToast(requireContext(), getString(R.string.error_add_sales))
            }
        getControlMonthSales()
    }

    // UPDATE PRODUCTS QUANTITY IN STOCK -------------------------------------------------
    private fun updateStock() {
        itemsSales.forEach {
            findsalesquantity(it.barcode, it.qtySales)
        }
    }
    private fun findsalesquantity(barcode: String, qtySales: Int) {
        var qtyactual = 0
        db.collection(dbProducts).document(barcode).get().addOnSuccessListener { task ->

            if (task.data != null && task.exists()) {
                val item = task.toObject(Product::class.java)
                val qty = item?.quantity ?: 0
                qtyactual = qty - qtySales
                updateStockQuantity(barcode, qtyactual)
            }
        }.addOnFailureListener { error ->
            Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateStockQuantity(barcode: String, newquantity: Int) {
        val value = hashMapOf(
            "quantity" to newquantity,
        )

        db.collection(dbProducts).document(barcode).update(value as Map<String, Any>)
            .addOnSuccessListener()
            {
            }.addOnFailureListener()
            {
                Util.exibirToast(requireContext(), "error to update products quantity")
            }
    }

    // ------------------------------------------------------------------------------------------------
    private fun getControlMonthSales() {

//        var monthList = listOf<Double>()
        when (calendar.get(Calendar.MONTH) + 1) {
            1 -> mes = "jan"
            2 -> mes = "feb"
            3 -> mes = "mar"
            4 -> mes = "apr"
            5 -> mes = "may"
            6 -> mes = "jun"
            7 -> mes = "jul"
            8 -> mes = "aug"
            9 -> mes = "sep"
            10 -> mes = "oct"
            11 -> mes = "nov"
            12 -> mes = "dec"
        }
        db.collection(dbMonthSales).document(calendar.get(Calendar.YEAR).toString()).get()
            .addOnSuccessListener { task ->
                if (task != null && task.exists()) {
                    actualvalue = task.getDouble(mes) ?: 0.0
                    updateControlMonthSales()
                } else {
                    actualvalue = 0.0
                    insertControlMonthSales()
                }
            }.addOnFailureListener { error ->
                dialogProgress.dismiss()
                "Erro de comunicação com servidor ${error.message.toString()}"
            }
    }

    private fun updateControlMonthSales() {
        actualvalue += (soma - discount)
        val value = hashMapOf(
            mes to actualvalue,
        )
        db.collection(dbMonthSales).document(calendar.get(Calendar.YEAR).toString()).update(value as Map<String, Any>)
            .addOnSuccessListener()
            {
                dialogProgress.dismiss()
            }.addOnFailureListener()
            {
                Util.exibirToast(requireContext(), "erro to upload month valeu")
                dialogProgress.dismiss()
            }
    }

    private fun insertControlMonthSales() {
        actualvalue += (soma - discount)
        val value = hashMapOf(
            mes to actualvalue,
        )
        db.collection(dbMonthSales).document(calendar.get(Calendar.YEAR).toString()).set(value)
            .addOnCompleteListener()
            {
                dialogProgress.dismiss()
            }.addOnFailureListener()
            {
                Util.exibirToast(requireContext(), "erro to insert month valeu")
                dialogProgress.dismiss()
            }
    }

    // FIRESTORE ________________________________

    private fun getItem(barcode: String) {

        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        db.collection(dbProducts).document(barcode).get().addOnSuccessListener { task ->

            dialogProgress.dismiss()
            if (task != null && task.exists()) {
//                val key = task.id // pegar o nome  da pasta do documento
                val dados = task.data
                val item = task.toObject(ItensSales::class.java)
                if (item != null) {
                    item.qtySales = qty
                    itemsSales.add(item)
                    itemsSalesAdapter.notifyDataSetChanged()
                    soma = 0.0
                    itemsSales.forEach {
                        soma += (it.salesPrice * it.qtySales)
                    }

                    binding.txtTotalPrice.text = String.format("%.2f", soma)
                    discountCalc()

                    binding.edtBarcode.setText("")
                    binding.edtBarcode.requestFocus()
                    parcelCalc()
                    qty = 1
                    updateQuantity()
                }
            } else {
                binding.edtBarcode.requestFocus()
                binding.edtBarcode.selectAll()
                qty = 1
                updateQuantity()
                Util.exibirToast(requireContext(), getString(R.string.error_show_product))
            }
        }.addOnFailureListener { error ->
            dialogProgress.dismiss()
            Util.exibirToast(requireContext(), getString(R.string.error_show_product) + ":" + error.message.toString())
        }
    }

    private fun getClient(client: String) {

        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        db.collection(dbClients).document(client).get().addOnSuccessListener { task ->

            dialogProgress.dismiss()
            if (task != null && task.exists()) {
//                val key = task.id // pegar o nome  da pasta do documento
                val dados = task.data
                val item = task.toObject(ItensSales::class.java)
                if (item != null) {

                    this.client = dados?.get("name").toString()
//                    Log.d("GETCLIENT", this.client)
//                    binding.txtClient.setText("")
//                    binding.txtClient.setText(dados?.get("name").toString())
                }
            } else {
                Util.exibirToast(requireContext(), getString(R.string.error_show_client))
            }
        }.addOnFailureListener { error ->
            dialogProgress.dismiss()
            getString(R.string.error_show_client) + ":" + error.message.toString()
        }
    }

    // --------------------------------------------------------------------

    private fun initAdapter() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setHasFixedSize(true)
        itemsSalesAdapter = ItensSalesAdapter(requireContext(), itemsSales, qty) { item ->
            // ao apagar um item da lista executa abaixo
            soma = 0.0
            item.forEach {
                soma += (it.salesPrice * it.qtySales)
            }
            binding.txtTotalPrice.text = String.format("%.2f", soma)
            discountCalc()
            parcelCalc()
        }
        binding.recyclerView.adapter = itemsSalesAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun informationDialog() {

        val builder = AlertDialog.Builder(requireContext())

        //set title for alert dialog
        builder.setTitle(Html.fromHtml("<font color='#F92391'>" + getString(R.string.attention) + "</font>"));
//        builder.setTitle("Atenção")

        //set message for alert dialog
        builder.setMessage(getString(R.string.finalize_sales))
        builder.setIcon(R.drawable.ic_rosa_round)

        //performing positive action
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            // cria função pegar a lista de
            insertSales()
        }
//        //performing cancel action
//        builder.setNeutralButton("Cancel"){dialogInterface , which ->
//            Toast.makeText(applicationContext,"clicked cancel\n operation cancel",Toast.LENGTH_LONG).show()
//        }
        //performing negative action
        builder.setNegativeButton(getString(R.string.no)) { _, _ ->
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
        val result = dialogLayout.findViewById<TextView>(R.id.txt_result)
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
            setTitle(Html.fromHtml("<font color='#F92391'>" + getString(R.string.search_customer_sales) + "</font>"))
            setPositiveButton(getString(R.string.ok)) { _, _ ->
                binding.txtClient.setText(client)
            }
            setNegativeButton(getString(R.string.cancel)) { _, _ ->

            }
            setView(dialogLayout)
            show()
        }
    }

    private fun getQuantityDialog() {

        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.item_dialog_get_qty, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.edt_qty)
        val buttonAdd = dialogLayout.findViewById<ImageView>(R.id.imv_add)
        val buttonSub = dialogLayout.findViewById<ImageView>(R.id.imv_sub)
        var qtyLocal = 1;

        editText.requestFocus()

        buttonAdd.setOnClickListener {
            qtyLocal += 1
            editText.setText(qtyLocal.toString())
        }

        buttonSub.setOnClickListener {
            if (qtyLocal >= 1) qtyLocal -= 1
            editText.setText(qtyLocal.toString())
        }

        with(builder) {
            setTitle(Html.fromHtml("<font color='#F92391'>" + "Add the Quantity" + "</font>"))
            setPositiveButton(getString(R.string.ok)) { _, _ ->
                qty = qtyLocal
                updateQuantity()
            }
            setNegativeButton(getString(R.string.cancel)) { _, _ ->

            }
            setView(dialogLayout)
            show()
        }
    }

    private fun updateQuantity() {
        binding.txtQuantityAdd.text = qty.toString()
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
}